# Usage example

```java
import io.github.pantheooon.mission.Mission;
import io.github.pantheooon.mission.MissionExecutedResult;

class DemoMission implements Mission {

    private String orderId;

    private String userId;

    public MissionExecutedResult execute() {

        //doSth

        return MissionExecutedResult(false,"执行失败...");
    }

}


```