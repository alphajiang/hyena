# Hyena


[![Travis (.org) branch](https://img.shields.io/travis/alphajiang/hyena/master.svg)](https://travis-ci.org/alphajiang/hyena)
[![Coveralls github branch](https://img.shields.io/coveralls/github/alphajiang/hyena/master.svg)](https://coveralls.io/github/alphajiang/hyena?branch=master)
[![Maven](https://img.shields.io/maven-central/v/io.github.alphajiang/hyena-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.alphajiang)
[![License](https://img.shields.io/github/license/alphajiang/hyena.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

用户余额/积分微服务
## 积分相关接口
### 增加积分
+ URL: /hyena/point/increase
+ Http Method: Post
+ Content-Type: application/json;charset=utf-8

<b>请求参数</b>

| 参数名 | 类型 | 含义 | 备注 |
| :-- | :-- | :-- | :-- |
| seq | string | 请求序列号. 1, 用于匹配请求消息和响应消息; 2, 做接口幂等性校验. 序列号为空时表示不做匹配及幂等性校验. | 每次新的请求使用不同的随机字串. 如果是重送请求使用相同的序列号 |
| type | string | 积分类型 | 可自定义类型, 用于存储'积分', '余额', 'XX币'等. |
| expireTime | string | 过期时间. 不传表示永不过期. | 格式为 "yyyy-MM-dd HH:mm:ss", 如: 2018-10-25 18:34:32 表示2018年10月25日18点34分32秒过期 |
 

### 冻结积分
/hyena/point/freeze
### 解冻积分
/hyena/point/unfreeze
### 使用积分
/hyena/point/decrease
### 使用已冻结积分
/hyena/point/decreaseFrozen
### 撤销积分
/hyena/point/cancel
### 获取用户积分列表
/hyena/point/listPoint
### 获取积分明细列表
/hyena/point/listPointRecord

## 示例代码
Maven
```
<dependency>
    <groupId>io.github.alphajiang</groupId>
    <artifactId>hyena-spring-boot-starter</artifactId>
    <version>0.0.3</version>
</dependency>
```
Gradle
```
plugins {
	id 'org.springframework.boot' version '2.1.4.RELEASE'
	id 'java'
}
apply plugin: 'io.spring.dependency-management'
dependencies {
    implementation("io.github.alphajiang:hyena-spring-boot-starter:0.0.3")
	implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:2.0.1'
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-jdbc")

	runtimeOnly 'mysql:mysql-connector-java'
}
```
Java代码
```
@SpringBootApplication
@ComponentScan({ "io.github.alphajiang.hyena" })
@MapperScan(basePackages = { "io.github.alphajiang.hyena.ds.mapper" })
@EnableTransactionManagement
@EnableScheduling
public class HyenaMain {
    public static void main(String[] args) {
        new SpringApplicationBuilder(HyenaMain.class).web(WebApplicationType.SERVLET).run(args);
    }
}
```


  

