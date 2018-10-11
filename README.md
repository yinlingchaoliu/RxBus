
#### 一、重写RxBus原因
```
市面上开源的RxBus并没有完全满足三点
1、EventBus式使用方法，无学习成本
2、功能不全：支持黏性事件，线程安全
3、完美替换EventBus
```

#### 二、重写思路
```
1、EventBus式使用方法，无学习成本
2、rxjava是观察者模式，可以替换Eventbus消息队列和处理消息的策略模式
```

#### eventbus源代码分析
https://www.jianshu.com/p/7dc9a097090d

#### 定制RxBus2源代码
https://github.com/yinlingchaoliu/RxBus

## Add dependency

#### step 1: 
Add it in your root build.gradle at the end of repositories:
```
allprojects {
	repositories {
		maven { url 'https://jitpack.io' }
	}
}
```
#### step 2:
```
dependencies {
        implementation 'com.github.yinlingchaoliu:RxBus:1.0.0'
}
```

## Usage

#### step 1: register
```java
RxBus.getDefault().register(this);
```

#### step 2: post & receive
```java
//post
RxBus.getDefault().post(new EventMain());

//receive
@Subscribe
public void receiveEventMain(EventMain event) {
    ((TextView) findViewById(R.id.tv_main_eventMsg)).setText(event.getMsg());
}
```
#### step 3: unregister
```java
    RxBus.getDefault().unregister(this);
    RxBus.getDefault().unregister(this,eventA);
```

## 特别感谢

1、重点参考优点：EventBus使用方式，缺点：功能不够完善，非线程安全！！！
[写框架】基于RxJava2，高仿EventBus打造RxBus2](https://www.jianshu.com/p/1fb3bfa7c427)
[RxBus2源码](https://github.com/KingJA/RxBus2)

2、优点：稳定，性能好。缺点:使用rxjava系列框架，建议改用rxbus
[EventBus源代码](https://github.com/greenrobot/EventBus)

3、优点：功能全面，代码少   缺点：还需要编写特定业务Manange类
[这个 RxBus 稳如老狗](https://blankj.com/2018/05/09/awesome-rxbus/)
[RxBus源代码](https://github.com/Blankj/RxBus)


## License

    Copyright 2018 Caliburn

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
