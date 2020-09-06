# README

本项目用于记录学习imooc课程 [企业级Android应用架构设计与开发](https://coding.imooc.com/class/364.html) 的学习笔记

**学习方式：**
* 看课程视频
* 下载课程代码，用 **Sublime merge** 查看各个章节的修改，关键模块的代码自己手动敲一遍
* 每章学完后进行总结，总结的时候思考怎样在简历上体现以及面试问到后怎样回答



### lib_network

lib_network 组件是对网络请求的封装，目的是为不同的业务提供统一的网络请求接口，避免各个业务自己实现，减少代码冗余，同时也是为了让业务模块能够与第三方的网络框架解耦，让业务不依赖特定的第三方组件。

**技术选型：**

由于目前Android领域已经有了非常成熟的网络框架，我们这次是针对优秀网络框架的二次封装。最终选择的是**OkHttp**， 因为它是目前最主流的网络框架，而且具有以下特点：

* 功能齐全
* 高性能
* 代码优秀，各种设计模式的运用（Singleton，Builder，策略模式，拦截器模式等等）

**封装思路：**

一次完整的网络请求主要包含三个部分：请求，响应和发送。因此我们的封装也是按照这三个部分来实现的。

* CommonRequest：封装 get/post/文件上传请求的构建
* CommonResponse：封装JsonCallback，供UI层获取数据和更新界面；封装OkHttpException，提高定位效率；封装FileCallback，处理文件下载请求并实现下载进度更新
* OkHttpClient：封装一个单例，用于发送post/get/文件下载请求并做一些通用的设置，例如校验hostname，添加公共请求头，设置超时时间等等，后续可以灵活扩展，通过添加setter方法让业务根据需要设置参数。

### lib_image_loader
lib_image_loader 组件是对图片加载功能的封装，为各业务模块提供统一的图片加载体验，并解耦业务模块对第三方库的依赖。

**技术选型：**

常见的图片加载库有 Volley， Picasso，Glide。

* Volley：Volley是 Google推出的库，但是近些年发展不太好。
* Picasso：Picasso 是非常流行的图片加载框架，对内存的优化非常好，它会绑定Activity的声明周期，当Activity销毁时回收图片内存；而且Picasso支持链式调用，使用很方便，会使代码看上去十分简洁。
* Glide：Glide 和 Picasso是同一家公司出品，是目前最流行的图片加载库，也得到了Google的推荐。Glide是基于 Picasso的，因此它继承了Picasso所有的优点，并且更加强大。

因此我们最终选择 Glide 作为我们底层的图片加载框架。

**封装思路：**

创建单例类 ImageLoaderManager，在里面实现各种业务层常用的图片加载方法，包括以下几种：

* 为普通ImageView加载图片
* 为ViewGroup加载图片 -- 大图加载优化，用RxJava将耗时部分放到IO线程执行，完成后回调主线程设置图片资源
* 加载圆形图片
* 为Notification的RemoteViews加载图片
* ......

有了 ImageLoaderManager 对各个图片加载场景的封装，以后如果还有其它场景，可以继续在这个类里扩展；业务层调用 ImageLoaderManager 完全不会感知底层的图片加载框架，这样以后如果更换性能更好地框架，只需修改 lib_image_loader 组件即可，业务层不需要做任何修改












