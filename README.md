# Maven

```
<dependency>
  <groupId>io.github.pantheooon</groupId>
  <artifactId>compensation-mission</artifactId>
  <version>1.1.0</version>
</dependency>
```

# Example

```java
public class MissionDemoController {

    @Autowired
    private MissionEngine engine;



    @GetMapping(value = "/demo")
    public void demo(String orderId){
        MyMission myMission = new MyMission();
        myMission.setOrderId(orderId);
        MissionExecutedResult missionExecutedResult = engine.executeMission(myMission);
    }

}

@MissionMeta(missionCode = "demo_mission",retryTimes = 7,gapTime = 30,gapTimeUnit = TimeUnit.SECONDS,retryType = RetryType.EXPONENT)
public class MyMission implements Mission {



    private String orderId;

    //无参构造必须要有
    public MyMission(){

    }

    public MyMission(String orderId) {
        this.orderId = orderId;
    }

    @NotNull
    @Override
    public MissionExecutedResult execute() {


        //have a remote call using orderId


        return new MissionExecutedResult(false,"remote call failure");
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
```

将需要补偿的任务封装成 `Mission`的实现类,在 `execute`	方法里执行业务逻辑,最终告诉引擎是执行成功还是失败了,如果失败了会将该任务序列化到存储层(可以是mysql或者内存)一遍下一次补偿执行,补偿方法入口需要调用 `MissionEngine.compensate()`.

同时支持补偿失败后如果需要支持回滚的模式,任务不再继承 `Mission`,而是 `RollbackAbleMission`,在 `rollback`方法里完成回滚的逻辑.

最后一个想说的是 `MissionMeta`,这个注解主要就是定义了执行的策略,以及间隔时间,retryType主要是两种,一种是每次间隔的时间都相等,另外一种是指数级增长,比如间隔时间设置的事10s,第一次10s,第二次20s,第三次40s,第四次80s....