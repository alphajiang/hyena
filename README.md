# Hyena


[![Travis (.org) branch](https://img.shields.io/travis/alphajiang/hyena/master.svg)](https://travis-ci.org/alphajiang/hyena)
[![Maven](https://img.shields.io/maven-central/v/io.github.alphajiang/hyena-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.alphajiang)
[![License](https://img.shields.io/github/license/alphajiang/hyena.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

用户余额/积分微服务
## 积分相关接口
### 增加积分
/hyena/point/increase
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


  

