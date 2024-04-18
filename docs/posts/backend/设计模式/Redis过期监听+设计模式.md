> ## 设计模式的实际应用
>
> 本篇主要讲设计模式在企业中的实际应用

# 工厂模式+策略模式

## 1、需求

对于活动过期时间、推送时间，系统需要根据过期时间修改活动状态，而对于不同的业务，我们需要不同的处理逻辑。那个我们可以将不同的业务作为一个策略封装起来，而策略的调用入口统一化

## 2、解决方案

- 通过redis过期监听策略实现
- 通过MQ延迟消息/死信交换机实现

这里我讲一下redis过期监听策略解决方案

## 3、修改redis配置文件，打开过期监听模式

redis.conf配置

```bash
notify-keyspace-events Ex
```

## 4、编写策略抽象接口（策略调用入口）

```java
public interface ExpireStrategy {

    /**
     * 处理过期的key
     * @param message
     */
    void handleExpireKey(Message message);
}
```

## 5、编写策略的具体实现类

> 推送配置监听策略

```java
@Component(value = "noticePushExpireStrategy")
@RequiredArgsConstructor
public class NoticePushExpireStrategy implements ExpireStrategy {
    private final NoticePushConfigService noticePushConfigService;

    @Override
    public void handleExpireKey(Message message) {
        String expireKey = message.toString();
        if(expireKey.contains(NOTICE_PUSH_TIME_.getCode())) {
            String id = expireKey.substring(NOTICE_PUSH_TIME_.getCode().length()+1, expireKey.length()-1);
            //更改状态
            LambdaUpdateWrapper<NoticePushConfig> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(NoticePushConfig::getId, id)
                    .set(NoticePushConfig::getSendStatus, "03");
            noticePushConfigService.update(updateWrapper);
        }
    }
}
```

> 计划有效期监听策略

```java
@Component(value = "evaluationPlanExpireStrategy")
@RequiredArgsConstructor
public class EvaluationPlanExpireStrategy implements ExpireStrategy{
    private final EvaluationPlanService evaluationPlanService;
    private final RedisService redisService;
    private final SerialNumInfoService serialNumInfoService;

    @Override
    public void handleExpireKey(Message message) {
        //TODO：自己业务的处理逻辑
    }
}
```



> 虽然我们定义了很多策略，并且单独封装起来了，但是在监听过期key的方法中，还是存在耦合性问题，我们需要通过if去判断用哪个策略。但通过工厂方法模式，我们就能解决这个问题

## 6、编写过期key配置类

```java
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "expire.listener")
public class RedisExpireListenerConfig {

    private Map<String,String> types;
}
```

## 7、在application.yml中配置过期key和策略（bean名称）之间的关系

```yml
# 缓存过期监听
expire:
  listener:
    types:
      PLAN_START_EXPIRE_FLAG_@@@@: evaluationPlanExpireStrategy
      PLAN_END_EXPIRE_FLAG_@@@@: evaluationPlanExpireStrategy
      ACTIVITY_START_EXPIRE_FLAG_@@@@: discountActivityExpireStrategy
      ACTIVITY_END_EXPIRE_FLAG_@@@@: discountActivityExpireStrategy
      NOTICE_PUSH_TIME_@@@@: noticePushExpireStrategy
      ....
```

## 8、编写策略生成工厂

```java
@Component
public class RedisExpireListenerFactory implements ApplicationContextAware {

    private static Map<String, ExpireStrategy> strategyMap = new ConcurrentHashMap<>();

    @Autowired
    private RedisExpireListenerConfig redisExpireListenerConfig;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        redisExpireListenerConfig.getTypes().forEach((k, y) -> {
            strategyMap.put(k, (ExpireStrategy) applicationContext.getBean(y));
        });
    }

    /**
     * 对外提供获取具体策略
     *
     * @return 具体策略
     */
    public ExpireStrategy getExpireStrategy(String expireKey) {
        //判断expireKey是否包含strategyMap字符串
        if(expireKey.indexOf("@@@@") == -1) {
            return null;
        }
        String prefix = expireKey.substring(0, expireKey.indexOf("@@@@") + 4);
        if (expireKey.contains(prefix)) {
            return strategyMap.get(prefix);
        }
        return null;
    }
}
```

## 9、编写Redis过期监听类

```java
@Service
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {

    @Autowired
    private RedisExpireListenerFactory redisExpireListenerFactory;

    @Value("${spring.redis.database}")
    private String database;

    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    @Override
    protected void doRegister(RedisMessageListenerContainer listenerContainer) {
        listenerContainer.addMessageListener(this, new PatternTopic("__keyevent@"+database+"__:expired"));
    }

    /**
     * 过期消息
     * @param message key
     * @param pattern 消息事件
     * @return void
     */
    @Override
    @Transactional
    public void onMessage(Message message, byte[] pattern) {
        String expireKey = message.toString();
        ExpireStrategy expireStrategy = redisExpireListenerFactory.getExpireStrategy(expireKey);
        if(expireStrategy != null) {
            expireStrategy.handleExpireKey(message);
        }
    }
}
```

## 10、拓展

当我们需要再对优惠券状态做监听时，只需要写一个ExpireStrategy的实现类，并在yml文件中配置key和bean名称的映射关系即可。

**注：redis过期监听策略需要考虑项目的实际情况，如果项目有大量的key会设置过期时间的，那么对redis开启过期监听策略，所有的key失效后都会走这个方法进行判断。**