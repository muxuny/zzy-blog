# 个人博客系统 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a personal blog website with Spring Boot 2.7.10 backend + Vue 3 frontend, supporting article management, Markdown editing, image upload, tag filtering, user registration with admin approval, and admin dashboard.

**Architecture:** Monolithic REST API backend (Spring Boot) with separate Vue 3 SPA frontend. Backend handles auth (JWT), business logic, file uploads. Frontend communicates via Axios. Nginx serves frontend static files and proxies API requests in production.

**Tech Stack:** Java 8, Spring Boot 2.7.10, MyBatis-Plus 3.5.x, MySQL, JWT, Hutool, Vue 3, Element Plus, Pinia, Axios, marked, highlight.js

---

## File Structure

### Backend (`blog-backend/` - transformed from `person-boot-test/`)

```
src/main/java/com/blog/
├── BlogApplication.java
├── config/
│   ├── WebMvcConfig.java          # CORS + static resources
│   ├── MyBatisPlusConfig.java     # MP pagination + meta-object handler
│   ├── FileUploadConfig.java      # Upload max size, path
│   ├── SecurityConfig.java        # Spring Security filter chain
│   └── JwtUtil.java               # JWT create/parse
├── common/
│   ├── BaseEntity.java            # Snowflake ID + audit fields
│   ├── Result.java                # Unified JSON response
│   ├── PageResult.java            # Paginated response
│   ├── SnowflakeIdUtil.java       # Snowflake ID generator via Hutool
│   ├── GlobalExceptionHandler.java
│   └── BusinessException.java
├── entity/
│   ├── User.java
│   ├── Article.java
│   ├── Tag.java
│   ├── ArticleTag.java
│   └── Image.java
├── mapper/
│   ├── UserMapper.java
│   ├── ArticleMapper.java
│   ├── TagMapper.java
│   ├── ArticleTagMapper.java
│   └── ImageMapper.java
├── service/                        # Interfaces
│   ├── UserService.java
│   ├── ArticleService.java
│   ├── TagService.java
│   └── ImageService.java
├── service/impl/
│   ├── UserServiceImpl.java
│   ├── ArticleServiceImpl.java
│   ├── TagServiceImpl.java
│   └── ImageServiceImpl.java
├── dto/
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   ├── ArticleRequest.java
│   └── ArticlePageQuery.java
├── controller/
│   ├── AuthController.java
│   ├── ArticleController.java       # Public article API
│   └── TagController.java           # Public tag API
└── controller/admin/
    ├── AdminArticleController.java
    ├── AdminTagController.java
    ├── AdminUserController.java
    └── ImageController.java

src/main/resources/
├── application.yml
└── db/
    └── init.sql

src/test/java/com/blog/
├── service/
│   ├── UserServiceTest.java
│   ├── ArticleServiceTest.java
│   └── TagServiceTest.java
└── controller/
    ├── AuthControllerTest.java
    └── ArticleControllerTest.java
```

### Frontend (`blog-frontend/` - new project)

```
blog-frontend/
├── index.html
├── package.json
├── vite.config.js
├── src/
│   ├── main.js
│   ├── App.vue
│   ├── router/
│   │   └── index.js
│   ├── api/
│   │   ├── request.js          # Axios instance + interceptors
│   │   ├── auth.js
│   │   ├── article.js
│   │   ├── tag.js
│   │   ├── user.js
│   │   └── image.js
│   ├── stores/
│   │   ├── auth.js             # User auth state
│   │   └── theme.js            # Light/dark theme
│   ├── styles/
│   │   ├── global.css
│   │   └── theme.css           # CSS variables for both themes
│   ├── utils/
│   │   └── index.js
│   ├── views/
│   │   ├── Home.vue
│   │   ├── ArticleDetail.vue
│   │   ├── TagArticles.vue
│   │   ├── Login.vue
│   │   ├── Register.vue
│   │   └── admin/
│   │       ├── Dashboard.vue
│   │       ├── Articles.vue
│   │       ├── ArticleEdit.vue
│   │       ├── Tags.vue
│   │       ├── Images.vue
│   │       ├── Users.vue
│   │       └── Profile.vue
│   └── components/
│       ├── AppHeader.vue
│       ├── ArticleCard.vue
│       ├── MarkdownRenderer.vue
│       ├── ThemeToggle.vue
│       └── admin/
│           ├── AdminLayout.vue
│           ├── ArticleEditor.vue
│           └── ImageUploader.vue
```

---

### Task 1: Project scaffolding — rename and restructure

**Files:**
- Modify: `person-boot-test/pom.xml` → full rewrite
- Modify: `person-boot-test/src/main/java/org/example/personboottest/PersonBootTestApplication.java` → move to `com/blog/BlogApplication.java`
- Delete: `person-boot-test/src/main/java/org/example/personboottest/controller/TestController.java`
- Delete: `person-boot-test/src/test/java/org/example/personboottest/PersonBootTestApplicationTests.java`
- Create: `blog-backend/src/main/java/com/blog/BlogApplication.java`
- Create: `blog-backend/src/test/java/com/blog/BlogApplicationTests.java`
- Create: `blog-backend/src/main/resources/application.yml`
- Create: `blog-backend/` (all directory structure)

- [ ] **Step 1: Create directory structure**

Run:
```powershell
$base = "D:\03PerProject\blog-backend"
$dirs = @(
    "src\main\java\com\blog\config",
    "src\main\java\com\blog\common",
    "src\main\java\com\blog\entity",
    "src\main\java\com\blog\mapper",
    "src\main\java\com\blog\service\impl",
    "src\main\java\com\blog\dto",
    "src\main\java\com\blog\controller\admin",
    "src\main\resources\db",
    "src\test\java\com\blog\service",
    "src\test\java\com\blog\controller"
)
foreach ($d in $dirs) { New-Item -ItemType Directory -Path "$base\$d" -Force | Out-Null }
```
Expected: directories created silently.

- [ ] **Step 2: Write `pom.xml`**

Write `D:\03PerProject\blog-backend\pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.10</version>
        <relativePath/>
    </parent>

    <groupId>com.blog</groupId>
    <artifactId>blog-backend</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>blog-backend</name>
    <description>Personal Blog Backend</description>

    <properties>
        <java.version>1.8</java.version>
    </properties>

    <dependencies>
        <!-- Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Security -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>

        <!-- Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- MyBatis-Plus -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
            <version>3.5.3.1</version>
        </dependency>

        <!-- MySQL -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>0.11.5</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>0.11.5</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>0.11.5</version>
            <scope>runtime</scope>
        </dependency>

        <!-- Hutool -->
        <dependency>
            <groupId>cn.hutool</groupId>
            <artifactId>hutool-all</artifactId>
            <version>5.8.22</version>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 3: Write `BlogApplication.java`**

Write `D:\03PerProject\blog-backend\src\main\java\com\blog\BlogApplication.java`:

```java
package com.blog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BlogApplication {
    public static void main(String[] args) {
        SpringApplication.run(BlogApplication.class, args);
    }
}
```

- [ ] **Step 4: Write `application.yml`**

Write `D:\03PerProject\blog-backend\src\main\resources\application.yml`:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/blog?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver

  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 20MB

mybatis-plus:
  global-config:
    db-config:
      id-type: none    # 不使用自增，由代码生成
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

blog:
  upload-dir: ${user.dir}/uploads
  jwt:
    secret: "aGVsbG8td29ybGQtYmxvZy1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tMjAyNg==" # Base64 min 256 bits
    expiration: 86400000  # 24h in ms
```

- [ ] **Step 5: Delete old project files**

Delete old source and test files (not needed after migration):
```powershell
Remove-Item -Recurse -Force "D:\03PerProject\person-boot-test"
```

- [ ] **Step 6: Copy .gitignore and create uploads dir**

```powershell
Copy-Item "D:\03PerProject\person-boot-test\.gitignore" "D:\03PerProject\blog-backend\.gitignore" -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Path "D:\03PerProject\uploads" -Force | Out-Null
```

- [ ] **Step 7: Verify build compiles**

Run: `cd D:\03PerProject\blog-backend && mvn compile -q`
Expected: BUILD SUCCESS (no errors)

---

### Task 2: Base infrastructure — common classes

**Files:**
- Create: `blog-backend/src/main/java/com/blog/common/BaseEntity.java`
- Create: `blog-backend/src/main/java/com/blog/common/Result.java`
- Create: `blog-backend/src/main/java/com/blog/common/PageResult.java`
- Create: `blog-backend/src/main/java/com/blog/common/BusinessException.java`
- Create: `blog-backend/src/main/java/com/blog/common/GlobalExceptionHandler.java`
- Create: `blog-backend/src/main/java/com/blog/config/MyBatisPlusConfig.java`

- [ ] **Step 1: Write `BaseEntity.java`**

```java
package com.blog.common;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public abstract class BaseEntity implements Serializable {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField(fill = FieldFill.INSERT)
    private String createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.UPDATE)
    private String updatedBy;

    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    @Version
    @TableField("version")
    private Integer version;
}
```

- [ ] **Step 2: Write `Result.java`**

```java
package com.blog.common;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Result<T> {
    private int code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    public static <T> Result<T> success() {
        return new Result<>(200, "success", null);
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }

    public static <T> Result<T> unauthorized(String message) {
        return new Result<>(401, message, null);
    }

    public static <T> Result<T> forbidden(String message) {
        return new Result<>(403, message, null);
    }
}
```

- [ ] **Step 3: Write `PageResult.java`**

```java
package com.blog.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PageResult<T> {
    private int code;
    private String message;
    private List<T> data;
    private long total;
    private long page;
    private long size;

    public static <T> PageResult<T> success(IPage<T> page) {
        return new PageResult<>(200, "success", page.getRecords(),
                page.getTotal(), page.getCurrent(), page.getSize());
    }
}
```

- [ ] **Step 4: Write `BusinessException.java`**

```java
package com.blog.common;

public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message) {
        this(400, message);
    }

    public int getCode() { return code; }
}
```

- [ ] **Step 5: Write `GlobalExceptionHandler.java`**

```java
package com.blog.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAccessDenied(AccessDeniedException e) {
        return Result.forbidden("无权限访问");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidation(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg = fieldError != null ? fieldError.getDefaultMessage() : "参数校验失败";
        return Result.error(400, msg);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        log.error("Unexpected error", e);
        return Result.error(500, "服务器内部错误");
    }
}
```

- [ ] **Step 6: Write `MyBatisPlusConfig.java`**

```java
package com.blog.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;

@Configuration
public class MyBatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }

    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                String username = getCurrentUsername();
                this.strictInsertFill(metaObject, "createdBy", String.class, username);
                this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
                this.strictInsertFill(metaObject, "updatedBy", String.class, username);
                this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
                this.strictInsertFill(metaObject, "deleted", Integer.class, 0);
                this.strictInsertFill(metaObject, "version", Integer.class, 0);
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                this.strictUpdateFill(metaObject, "updatedBy", String.class, getCurrentUsername());
                this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
            }

            private String getCurrentUsername() {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.isAuthenticated()
                        && !"anonymousUser".equals(auth.getPrincipal())) {
                    return auth.getName();
                }
                return "system";
            }
        };
    }
}
```

- [ ] **Step 7: Compile to verify**

Run: `cd D:\03PerProject\blog-backend && mvn compile -q`
Expected: BUILD SUCCESS

---

### Task 3: Database init SQL

**Files:**
- Create: `blog-backend/src/main/resources/db/init.sql`

- [ ] **Step 1: Write `init.sql`**

```sql
CREATE DATABASE IF NOT EXISTS blog DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE blog;

CREATE TABLE `user` (
    `id` BIGINT NOT NULL COMMENT '雪花ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT 'BCrypt加密密码',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `role` VARCHAR(20) NOT NULL DEFAULT 'user' COMMENT '角色: admin/user',
    `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态: pending/active/disabled',
    `created_by` VARCHAR(50) DEFAULT NULL,
    `created_at` DATETIME DEFAULT NULL,
    `updated_by` VARCHAR(50) DEFAULT NULL,
    `updated_at` DATETIME DEFAULT NULL,
    `deleted` TINYINT(1) DEFAULT '0' COMMENT '逻辑删除',
    `version` INT DEFAULT '0' COMMENT '乐观锁',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE `article` (
    `id` BIGINT NOT NULL COMMENT '雪花ID',
    `title` VARCHAR(200) NOT NULL COMMENT '标题',
    `content` LONGTEXT COMMENT 'Markdown内容',
    `summary` VARCHAR(500) DEFAULT NULL COMMENT '摘要',
    `cover_image` VARCHAR(255) DEFAULT NULL COMMENT '封面图URL',
    `status` VARCHAR(20) NOT NULL DEFAULT 'draft' COMMENT '状态: draft/published',
    `view_count` INT DEFAULT '0' COMMENT '阅读数',
    `created_by` VARCHAR(50) DEFAULT NULL,
    `created_at` DATETIME DEFAULT NULL,
    `updated_by` VARCHAR(50) DEFAULT NULL,
    `updated_at` DATETIME DEFAULT NULL,
    `deleted` TINYINT(1) DEFAULT '0' COMMENT '逻辑删除',
    `version` INT DEFAULT '0' COMMENT '乐观锁',
    PRIMARY KEY (`id`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章表';

CREATE TABLE `tag` (
    `id` BIGINT NOT NULL COMMENT '雪花ID',
    `name` VARCHAR(50) NOT NULL COMMENT '标签名',
    `created_by` VARCHAR(50) DEFAULT NULL,
    `created_at` DATETIME DEFAULT NULL,
    `updated_by` VARCHAR(50) DEFAULT NULL,
    `updated_at` DATETIME DEFAULT NULL,
    `deleted` TINYINT(1) DEFAULT '0' COMMENT '逻辑删除',
    `version` INT DEFAULT '0' COMMENT '乐观锁',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';

CREATE TABLE `article_tag` (
    `id` BIGINT NOT NULL COMMENT '雪花ID',
    `article_id` BIGINT NOT NULL COMMENT '文章ID',
    `tag_id` BIGINT NOT NULL COMMENT '标签ID',
    `created_by` VARCHAR(50) DEFAULT NULL,
    `created_at` DATETIME DEFAULT NULL,
    `updated_by` VARCHAR(50) DEFAULT NULL,
    `updated_at` DATETIME DEFAULT NULL,
    `deleted` TINYINT(1) DEFAULT '0' COMMENT '逻辑删除',
    `version` INT DEFAULT '0' COMMENT '乐观锁',
    PRIMARY KEY (`id`),
    KEY `idx_article_id` (`article_id`),
    KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章标签关联表';

CREATE TABLE `image` (
    `id` BIGINT NOT NULL COMMENT '雪花ID',
    `original_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `filename` VARCHAR(255) NOT NULL COMMENT '存储文件名(UUID)',
    `size` BIGINT NOT NULL DEFAULT '0' COMMENT '文件大小(字节)',
    `mime_type` VARCHAR(50) DEFAULT NULL COMMENT 'MIME类型',
    `url` VARCHAR(255) NOT NULL COMMENT '访问路径',
    `created_by` VARCHAR(50) DEFAULT NULL,
    `created_at` DATETIME DEFAULT NULL,
    `updated_by` VARCHAR(50) DEFAULT NULL,
    `updated_at` DATETIME DEFAULT NULL,
    `deleted` TINYINT(1) DEFAULT '0' COMMENT '逻辑删除',
    `version` INT DEFAULT '0' COMMENT '乐观锁',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图片记录表';
```

- [ ] **Step 2: Execute SQL**

Run: `mysql -u root -p < "D:\03PerProject\blog-backend\src\main\resources\db\init.sql"`
Expected: tables created. Enter password `root` when prompted.

---

### Task 4: Entity classes

**Files:**
- Create: `blog-backend/src/main/java/com/blog/entity/User.java`
- Create: `blog-backend/src/main/java/com/blog/entity/Article.java`
- Create: `blog-backend/src/main/java/com/blog/entity/Tag.java`
- Create: `blog-backend/src/main/java/com/blog/entity/ArticleTag.java`
- Create: `blog-backend/src/main/java/com/blog/entity/Image.java`

- [ ] **Step 1: Write `User.java`**

```java
package com.blog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blog.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity {
    private String username;
    private String password;
    private String nickname;
    private String avatar;
    private String email;
    private String role;
    private String status;
}
```

- [ ] **Step 2: Write `Article.java`**

```java
package com.blog.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.blog.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("article")
public class Article extends BaseEntity {
    private String title;
    private String content;
    private String summary;
    private String coverImage;
    private String status;
    private Integer viewCount;

    @TableField(exist = false)
    private List<Tag> tags;

    @TableField(exist = false)
    private String authorName;
}
```

- [ ] **Step 3: Write `Tag.java`**

```java
package com.blog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blog.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tag")
public class Tag extends BaseEntity {
    private String name;
}
```

- [ ] **Step 4: Write `ArticleTag.java`**

```java
package com.blog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blog.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("article_tag")
public class ArticleTag extends BaseEntity {
    private Long articleId;
    private Long tagId;
}
```

- [ ] **Step 5: Write `Image.java`**

```java
package com.blog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blog.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("image")
public class Image extends BaseEntity {
    private String originalName;
    private String filename;
    private Long size;
    private String mimeType;
    private String url;
}
```

- [ ] **Step 6: Compile**

Run: `cd D:\03PerProject\blog-backend && mvn compile -q`
Expected: BUILD SUCCESS

---

### Task 5: Mapper interfaces

**Files:**
- Create: `blog-backend/src/main/java/com/blog/mapper/UserMapper.java`
- Create: `blog-backend/src/main/java/com/blog/mapper/ArticleMapper.java`
- Create: `blog-backend/src/main/java/com/blog/mapper/TagMapper.java`
- Create: `blog-backend/src/main/java/com/blog/mapper/ArticleTagMapper.java`
- Create: `blog-backend/src/main/java/com/blog/mapper/ImageMapper.java`

- [ ] **Step 1: Write all mappers**

Write each file — all extend `BaseMapper<T>` from MyBatis-Plus.

`UserMapper.java`:
```java
package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.User;

public interface UserMapper extends BaseMapper<User> {
}
```

`ArticleMapper.java`:
```java
package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.Article;

public interface ArticleMapper extends BaseMapper<Article> {
}
```

`TagMapper.java`:
```java
package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.Tag;

public interface TagMapper extends BaseMapper<Tag> {
}
```

`ArticleTagMapper.java`:
```java
package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.ArticleTag;

public interface ArticleTagMapper extends BaseMapper<ArticleTag> {
}
```

`ImageMapper.java`:
```java
package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.Image;

public interface ImageMapper extends BaseMapper<Image> {
}
```

- [ ] **Step 2: Add `@MapperScan` to `BlogApplication.java`**

Edit `BlogApplication.java`:
```java
package com.blog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.blog.mapper")
public class BlogApplication {
    public static void main(String[] args) {
        SpringApplication.run(BlogApplication.class, args);
    }
}
```

- [ ] **Step 3: Compile**

Run: `cd D:\03PerProject\blog-backend && mvn compile -q`
Expected: BUILD SUCCESS

---

### Task 6: JWT utility + DTOs

**Files:**
- Create: `blog-backend/src/main/java/com/blog/config/JwtUtil.java`
- Create: `blog-backend/src/main/java/com/blog/dto/LoginRequest.java`
- Create: `blog-backend/src/main/java/com/blog/dto/RegisterRequest.java`
- Create: `blog-backend/src/main/java/com/blog/dto/ArticleRequest.java`
- Create: `blog-backend/src/main/java/com/blog/dto/ArticlePageQuery.java`

- [ ] **Step 1: Write `JwtUtil.java`**

```java
package com.blog.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {
    private final SecretKey secretKey;
    private final long expiration;

    public JwtUtil(@Value("${blog.jwt.secret}") String secret,
                   @Value("${blog.jwt.expiration}") long expiration) {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.expiration = expiration;
    }

    public String generateToken(String username, String role) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiration))
                .signWith(secretKey)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getUsername(String token) {
        return parseToken(token).getSubject();
    }

    public String getRole(String token) {
        return parseToken(token).get("role", String.class);
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
```

- [ ] **Step 2: Write `LoginRequest.java`**

```java
package com.blog.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class LoginRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
```

- [ ] **Step 3: Write `RegisterRequest.java`**

```java
package com.blog.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class RegisterRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度3-50")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度6-100")
    private String password;

    private String nickname;
    private String email;
}
```

- [ ] **Step 4: Write `ArticleRequest.java`**

```java
package com.blog.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
public class ArticleRequest {
    @NotBlank(message = "标题不能为空")
    private String title;

    private String content;
    private String summary;
    private String coverImage;
    private String status;

    private List<Long> tagIds;
}
```

- [ ] **Step 5: Write `ArticlePageQuery.java`**

```java
package com.blog.dto;

import lombok.Data;

@Data
public class ArticlePageQuery {
    private long page = 1;
    private long size = 10;
    private Long tagId;
    private String status;
    private String keyword;
}
```

- [ ] **Step 6: Compile**

Run: `cd D:\03PerProject\blog-backend && mvn compile -q`
Expected: BUILD SUCCESS

---

### Task 7: Security configuration — Spring Security + JWT filter

**Files:**
- Create: `blog-backend/src/main/java/com/blog/config/SecurityConfig.java`
- Create: `blog-backend/src/main/java/com/blog/config/WebMvcConfig.java`

- [ ] **Step 1: Write `SecurityConfig.java`**

```java
package com.blog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors().and().csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
            .antMatchers("/api/auth/**").permitAll()
            .antMatchers(HttpMethod.GET, "/api/articles/**").permitAll()
            .antMatchers(HttpMethod.GET, "/api/tags").permitAll()
            .antMatchers("/uploads/**").permitAll()
            .antMatchers("/api/admin/**").hasAnyRole("ADMIN", "USER")
            .anyRequest().authenticated()
            .and()
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
```

- [ ] **Step 2: Write `JwtAuthFilter.java`**

Note: This step requires creating an additional file first.

Create `blog-backend/src/main/java/com/blog/config/JwtAuthFilter.java`:

```java
package com.blog.config;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String token = extractToken(request);
        if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
            String username = jwtUtil.getUsername(token);
            String role = jwtUtil.getRole(token);

            SimpleGrantedAuthority authority =
                    new SimpleGrantedAuthority("ROLE_" + role.toUpperCase());

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            username, null, Collections.singletonList(authority));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
```

- [ ] **Step 3: Write `WebMvcConfig.java`**

```java
package com.blog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${blog.upload-dir}")
    private String uploadDir;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173", "http://localhost:8080")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}
```

- [ ] **Step 4: Compile**

Run: `cd D:\03PerProject\blog-backend && mvn compile -q`
Expected: BUILD SUCCESS

---

### Task 8: Service layer — User service

**Files:**
- Create: `blog-backend/src/main/java/com/blog/service/UserService.java`
- Create: `blog-backend/src/main/java/com/blog/service/impl/UserServiceImpl.java`

- [ ] **Step 1: Write `UserService.java`**

```java
package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.dto.LoginRequest;
import com.blog.dto.RegisterRequest;
import com.blog.entity.User;

import java.util.Map;

public interface UserService extends IService<User> {
    Map<String, Object> login(LoginRequest request);
    void register(RegisterRequest request);
    User getCurrentUser(String username);
    void approveUser(Long userId);
    void disableUser(Long userId);
}
```

- [ ] **Step 2: Write `UserServiceImpl.java`**

```java
package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.common.BusinessException;
import com.blog.config.JwtUtil;
import com.blog.dto.LoginRequest;
import com.blog.dto.RegisterRequest;
import com.blog.entity.User;
import com.blog.mapper.UserMapper;
import com.blog.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserServiceImpl(PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Map<String, Object> login(LoginRequest request) {
        User user = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));

        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (!"active".equals(user.getStatus())) {
            throw new BusinessException("账号未激活或被禁用");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        return result;
    }

    @Override
    public void register(RegisterRequest request) {
        User exist = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));
        if (exist != null) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setEmail(request.getEmail());
        user.setRole("user");
        user.setStatus("pending");
        save(user);
    }

    @Override
    public User getCurrentUser(String username) {
        return getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    @Override
    public void approveUser(Long userId) {
        User user = getById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        user.setStatus("active");
        updateById(user);
    }

    @Override
    public void disableUser(Long userId) {
        User user = getById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        user.setStatus("disabled");
        updateById(user);
    }
}
```

- [ ] **Step 3: Compile**

Run: `cd D:\03PerProject\blog-backend && mvn compile -q`
Expected: BUILD SUCCESS

---

### Task 9: Service layer — Article service

**Files:**
- Create: `blog-backend/src/main/java/com/blog/service/ArticleService.java`
- Create: `blog-backend/src/main/java/com/blog/service/impl/ArticleServiceImpl.java`

- [ ] **Step 1: Write `ArticleService.java`**

```java
package com.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.dto.ArticlePageQuery;
import com.blog.dto.ArticleRequest;
import com.blog.entity.Article;

public interface ArticleService extends IService<Article> {
    IPage<Article> getPublicPage(ArticlePageQuery query);
    Article getPublicDetail(Long id);
    Article createArticle(ArticleRequest request, String username);
    Article updateArticle(Long id, ArticleRequest request, String username);
    void deleteArticle(Long id, String username, String role);
    IPage<Article> getAdminPage(ArticlePageQuery query);
}
```

- [ ] **Step 2: Write `ArticleServiceImpl.java`**

```java
package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.common.BusinessException;
import com.blog.dto.ArticlePageQuery;
import com.blog.dto.ArticleRequest;
import com.blog.entity.Article;
import com.blog.entity.ArticleTag;
import com.blog.entity.Tag;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.ArticleTagMapper;
import com.blog.mapper.TagMapper;
import com.blog.service.ArticleService;
import com.blog.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    private final ArticleTagMapper articleTagMapper;
    private final TagMapper tagMapper;
    private final UserService userService;

    public ArticleServiceImpl(ArticleTagMapper articleTagMapper,
                              TagMapper tagMapper,
                              UserService userService) {
        this.articleTagMapper = articleTagMapper;
        this.tagMapper = tagMapper;
        this.userService = userService;
    }

    @Override
    public IPage<Article> getPublicPage(ArticlePageQuery query) {
        Page<Article> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, "published")
                .orderByDesc(Article::getCreatedAt);

        if (query.getTagId() != null) {
            List<Long> articleIds = articleTagMapper.selectList(
                    new LambdaQueryWrapper<ArticleTag>()
                            .eq(ArticleTag::getTagId, query.getTagId()))
                    .stream().map(ArticleTag::getArticleId).collect(Collectors.toList());
            if (articleIds.isEmpty()) {
                return page;
            }
            wrapper.in(Article::getId, articleIds);
        }
        if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
            wrapper.like(Article::getTitle, query.getKeyword());
        }

        IPage<Article> result = baseMapper.selectPage(page, wrapper);
        result.getRecords().forEach(this::attachTagsAndAuthor);
        return result;
    }

    @Override
    public Article getPublicDetail(Long id) {
        Article article = getById(id);
        if (article == null || "1".equals(article.getDeleted())) {
            throw new BusinessException("文章不存在");
        }
        // Increment view count
        article.setViewCount(article.getViewCount() == null ? 1 : article.getViewCount() + 1);
        updateById(article);
        attachTagsAndAuthor(article);
        return article;
    }

    @Override
    @Transactional
    public Article createArticle(ArticleRequest request, String username) {
        Article article = new Article();
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setSummary(request.getSummary());
        article.setCoverImage(request.getCoverImage());
        article.setStatus(request.getStatus() != null ? request.getStatus() : "draft");
        article.setViewCount(0);
        save(article);

        if (request.getTagIds() != null) {
            for (Long tagId : request.getTagIds()) {
                ArticleTag at = new ArticleTag();
                at.setArticleId(article.getId());
                at.setTagId(tagId);
                articleTagMapper.insert(at);
            }
        }
        attachTagsAndAuthor(article);
        return article;
    }

    @Override
    @Transactional
    public Article updateArticle(Long id, ArticleRequest request, String username) {
        Article article = getById(id);
        if (article == null) throw new BusinessException("文章不存在");

        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setSummary(request.getSummary());
        article.setCoverImage(request.getCoverImage());
        article.setStatus(request.getStatus());
        updateById(article);

        // Re-sync tags: delete all, insert new
        articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>()
                .eq(ArticleTag::getArticleId, id));
        if (request.getTagIds() != null) {
            for (Long tagId : request.getTagIds()) {
                ArticleTag at = new ArticleTag();
                at.setArticleId(id);
                at.setTagId(tagId);
                articleTagMapper.insert(at);
            }
        }
        attachTagsAndAuthor(article);
        return article;
    }

    @Override
    @Transactional
    public void deleteArticle(Long id, String username, String role) {
        Article article = getById(id);
        if (article == null) throw new BusinessException("文章不存在");
        // Only admin or owner can delete
        if (!"admin".equals(role) && !username.equals(article.getCreatedBy())) {
            throw new BusinessException("无权删除该文章");
        }
        removeById(id);
    }

    @Override
    public IPage<Article> getAdminPage(ArticlePageQuery query) {
        Page<Article> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
                .orderByDesc(Article::getCreatedAt);

        if (query.getStatus() != null && !query.getStatus().isEmpty()) {
            wrapper.eq(Article::getStatus, query.getStatus());
        }
        if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
            wrapper.like(Article::getTitle, query.getKeyword());
        }

        IPage<Article> result = baseMapper.selectPage(page, wrapper);
        result.getRecords().forEach(this::attachTagsAndAuthor);
        return result;
    }

    private void attachTagsAndAuthor(Article article) {
        // Get tags
        List<ArticleTag> ats = articleTagMapper.selectList(
                new LambdaQueryWrapper<ArticleTag>()
                        .eq(ArticleTag::getArticleId, article.getId()));
        if (!ats.isEmpty()) {
            List<Long> tagIds = ats.stream()
                    .map(ArticleTag::getTagId).collect(Collectors.toList());
            List<Tag> tags = tagMapper.selectBatchIds(tagIds);
            article.setTags(tags);
        }

        // Get author name
        if (article.getCreatedBy() != null) {
            var user = userService.getOne(
                    new LambdaQueryWrapper<com.blog.entity.User>()
                            .eq(com.blog.entity.User::getUsername, article.getCreatedBy()));
            if (user != null) {
                article.setAuthorName(user.getNickname());
            }
        }
    }
}
```

- [ ] **Step 2: Compile**

Run: `cd D:\03PerProject\blog-backend && mvn compile -q`
Expected: BUILD SUCCESS

---

### Task 10: Service layer — Tag + Image services

**Files:**
- Create: `blog-backend/src/main/java/com/blog/service/TagService.java`
- Create: `blog-backend/src/main/java/com/blog/service/impl/TagServiceImpl.java`
- Create: `blog-backend/src/main/java/com/blog/service/ImageService.java`
- Create: `blog-backend/src/main/java/com/blog/service/impl/ImageServiceImpl.java`

- [ ] **Step 1: Write `TagService.java`**

```java
package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.entity.Tag;
import java.util.List;

public interface TagService extends IService<Tag> {
    List<Tag> getAllTags();
    Tag createTag(String name);
}
```

`TagServiceImpl.java`:
```java
package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.common.BusinessException;
import com.blog.entity.Tag;
import com.blog.mapper.TagMapper;
import com.blog.service.TagService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    @Override
    public List<Tag> getAllTags() {
        return list(new LambdaQueryWrapper<Tag>().orderByAsc(Tag::getName));
    }

    @Override
    public Tag createTag(String name) {
        Tag exist = getOne(new LambdaQueryWrapper<Tag>().eq(Tag::getName, name));
        if (exist != null) throw new BusinessException("标签已存在");
        Tag tag = new Tag();
        tag.setName(name);
        save(tag);
        return tag;
    }
}
```

- [ ] **Step 2: Write `ImageService.java`**

```java
package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.entity.Image;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService extends IService<Image> {
    Image uploadImage(MultipartFile file, String username);
    void deleteImage(Long id);
}
```

`ImageServiceImpl.java`:
```java
package com.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.common.BusinessException;
import com.blog.entity.Image;
import com.blog.mapper.ImageMapper;
import com.blog.service.ImageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class ImageServiceImpl extends ServiceImpl<ImageMapper, Image> implements ImageService {

    @Value("${blog.upload-dir}")
    private String uploadDir;

    @Override
    public Image uploadImage(MultipartFile file, String username) {
        if (file.isEmpty()) throw new BusinessException("文件不能为空");

        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String filename = UUID.randomUUID().toString() + ext;

        try {
            File dest = new File(uploadDir, filename);
            if (!dest.getParentFile().exists()) dest.getParentFile().mkdirs();
            file.transferTo(dest);
        } catch (IOException e) {
            throw new BusinessException("文件上传失败");
        }

        Image image = new Image();
        image.setOriginalName(originalName);
        image.setFilename(filename);
        image.setSize(file.getSize());
        image.setMimeType(file.getContentType());
        image.setUrl("/uploads/" + filename);
        save(image);
        return image;
    }

    @Override
    public void deleteImage(Long id) {
        Image image = getById(id);
        if (image == null) throw new BusinessException("图片不存在");

        File file = new File(uploadDir, image.getFilename());
        if (file.exists()) file.delete();
        removeById(id);
    }
}
```

- [ ] **Step 3: Compile**

Run: `cd D:\03PerProject\blog-backend && mvn compile -q`
Expected: BUILD SUCCESS

---

### Task 11: Controllers — Auth + Tag (public)

**Files:**
- Create: `blog-backend/src/main/java/com/blog/controller/AuthController.java`
- Create: `blog-backend/src/main/java/com/blog/controller/TagController.java`

- [ ] **Step 1: Write `AuthController.java`**

```java
package com.blog.controller;

import com.blog.common.Result;
import com.blog.dto.LoginRequest;
import com.blog.dto.RegisterRequest;
import com.blog.entity.User;
import com.blog.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(userService.login(request));
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return Result.success();
    }

    @GetMapping("/me")
    public Result<User> me(Principal principal) {
        if (principal == null) {
            return Result.unauthorized("未登录");
        }
        User user = userService.getCurrentUser(principal.getName());
        user.setPassword(null);
        return Result.success(user);
    }
}
```

- [ ] **Step 2: Write `TagController.java`**

```java
package com.blog.controller;

import com.blog.common.Result;
import com.blog.entity.Tag;
import com.blog.service.TagService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public Result<List<Tag>> list() {
        return Result.success(tagService.getAllTags());
    }
}
```

- [ ] **Step 3: Compile**

Run: `cd D:\03PerProject\blog-backend && mvn compile -q`
Expected: BUILD SUCCESS

---

### Task 12: Controllers — Article (public)

**Files:**
- Create: `blog-backend/src/main/java/com/blog/controller/ArticleController.java`

- [ ] **Step 1: Write `ArticleController.java`**

```java
package com.blog.controller;

import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.dto.ArticlePageQuery;
import com.blog.dto.ArticleRequest;
import com.blog.entity.Article;
import com.blog.service.ArticleService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    public PageResult<Article> list(ArticlePageQuery query) {
        return PageResult.success(articleService.getPublicPage(query));
    }

    @GetMapping("/{id}")
    public Result<Article> detail(@PathVariable Long id) {
        return Result.success(articleService.getPublicDetail(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public Result<Article> create(@Valid @RequestBody ArticleRequest request,
                                   Principal principal) {
        return Result.success(articleService.createArticle(request, principal.getName()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public Result<Article> update(@PathVariable Long id,
                                   @Valid @RequestBody ArticleRequest request,
                                   Principal principal) {
        return Result.success(articleService.updateArticle(id, request, principal.getName()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public Result<Void> delete(@PathVariable Long id, Principal principal) {
        // We'll let the service layer check ownership since @PreAuthorize can't easily
        String role = getUserRole(principal);
        articleService.deleteArticle(id, principal.getName(), role);
        return Result.success();
    }

    private String getUserRole(Principal principal) {
        if (principal == null) return "anonymous";
        var auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")) ? "admin" : "user";
    }
}
```

- [ ] **Step 2: Compile**

Run: `cd D:\03PerProject\blog-backend && mvn compile -q`
Expected: BUILD SUCCESS

---

### Task 13: Controllers — Admin controllers

**Files:**
- Create: `blog-backend/src/main/java/com/blog/controller/admin/AdminArticleController.java`
- Create: `blog-backend/src/main/java/com/blog/controller/admin/AdminTagController.java`
- Create: `blog-backend/src/main/java/com/blog/controller/admin/AdminUserController.java`
- Create: `blog-backend/src/main/java/com/blog/controller/admin/ImageController.java`

- [ ] **Step 1: Write `AdminArticleController.java`**

```java
package com.blog.controller.admin;

import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.dto.ArticlePageQuery;
import com.blog.entity.Article;
import com.blog.service.ArticleService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/articles")
@PreAuthorize("hasRole('ADMIN')")
public class AdminArticleController {

    private final ArticleService articleService;

    public AdminArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    public PageResult<Article> list(ArticlePageQuery query) {
        return PageResult.success(articleService.getAdminPage(query));
    }
}
```

- [ ] **Step 2: Write `AdminTagController.java`**

```java
package com.blog.controller.admin;

import com.blog.common.Result;
import com.blog.entity.Tag;
import com.blog.service.TagService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/tags")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTagController {

    private final TagService tagService;

    public AdminTagController(TagService tagService) {
        this.tagService = tagService;
    }

    @PostMapping
    public Result<Tag> create(@RequestBody Map<String, String> body) {
        return Result.success(tagService.createTag(body.get("name")));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        tagService.removeById(id);
        return Result.success();
    }
}
```

- [ ] **Step 3: Write `AdminUserController.java`**

```java
package com.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.entity.User;
import com.blog.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public PageResult<User> list(@RequestParam(defaultValue = "1") long page,
                                  @RequestParam(defaultValue = "10") long size) {
        IPage<User> result = userService.page(new Page<>(page, size),
                new LambdaQueryWrapper<User>().orderByDesc(User::getCreatedAt));
        result.getRecords().forEach(u -> u.setPassword(null));
        return PageResult.success(result);
    }

    @GetMapping("/{id}")
    public Result<User> detail(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user != null) user.setPassword(null);
        return Result.success(user);
    }

    @PutMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id) {
        userService.approveUser(id);
        return Result.success();
    }

    @PutMapping("/{id}/disable")
    public Result<Void> disable(@PathVariable Long id) {
        userService.disableUser(id);
        return Result.success();
    }
}
```

- [ ] **Step 4: Write `ImageController.java`**

```java
package com.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.entity.Image;
import com.blog.service.ImageService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@RestController
@RequestMapping("/api/admin/upload")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping("/image")
    public Result<Image> upload(@RequestParam("file") MultipartFile file,
                                 Principal principal) {
        return Result.success(imageService.uploadImage(file, principal.getName()));
    }

    @DeleteMapping("/image/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        imageService.deleteImage(id);
        return Result.success();
    }

    @GetMapping("/images")
    public PageResult<Image> list(@RequestParam(defaultValue = "1") long page,
                                   @RequestParam(defaultValue = "20") long size) {
        IPage<Image> result = imageService.page(new Page<>(page, size),
                new LambdaQueryWrapper<Image>().orderByDesc(Image::getCreatedAt));
        return PageResult.success(result);
    }
}
```

- [ ] **Step 5: Compile**

Run: `cd D:\03PerProject\blog-backend && mvn compile -q`
Expected: BUILD SUCCESS

---

### Task 14: Backend unit tests

**Files:**
- Create: `blog-backend/src/test/java/com/blog/service/UserServiceTest.java`
- Create: `blog-backend/src/test/java/com/blog/service/ArticleServiceTest.java`
- Create: `blog-backend/src/test/java/com/blog/service/TagServiceTest.java`
- Create: `blog-backend/src/test/java/com/blog/controller/AuthControllerTest.java`
- Create: `blog-backend/src/test/java/com/blog/controller/ArticleControllerTest.java`
- Create: `blog-backend/src/test/java/com/blog/BlogApplicationTests.java`

- [ ] **Step 1: Write `BlogApplicationTests.java`**

```java
package com.blog;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BlogApplicationTests {
    @Test
    void contextLoads() {
    }
}
```

- [ ] **Step 2: Write `UserServiceTest.java`**

```java
package com.blog.service;

import com.blog.common.BusinessException;
import com.blog.dto.LoginRequest;
import com.blog.dto.RegisterRequest;
import com.blog.entity.User;
import com.blog.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(new BCryptPasswordEncoder(), null) {
            // Override to use mocked mapper
            @Override
            public User getOne(com.baomidou.mybatisplus.core.conditions.Wrapper<User> queryWrapper) {
                return userMapper.selectOne(queryWrapper);
            }
        };
    }

    @Test
    void register_shouldThrowWhenUsernameExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("123456");

        when(userMapper.selectOne(any())).thenReturn(new User());

        assertThrows(BusinessException.class, () -> userService.register(request));
    }
}
```

- [ ] **Step 3: Write `AuthControllerTest.java`**

```java
package com.blog.controller;

import com.blog.common.Result;
import com.blog.dto.LoginRequest;
import com.blog.dto.RegisterRequest;
import com.blog.entity.User;
import com.blog.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    @Test
    void login_shouldReturnToken() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("admin123");

        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("token", "test-token");
        mockResult.put("user", new User());
        when(userService.login(any())).thenReturn(mockResult);

        Result<Map<String, Object>> result = authController.login(request);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData().get("token"));
    }
}
```

- [ ] **Step 4: Run tests**

Run: `cd D:\03PerProject\blog-backend && mvn test -q`
Expected: BUILD SUCCESS, tests pass

- [ ] **Step 5: Commit backend**

```bash
cd D:\03PerProject\blog-backend
git init
git add .
git commit -m "feat: initialize blog backend with user/article/tag modules"
```

---

### Task 15: Frontend project setup

**Files:**
- Create: `blog-frontend/package.json`
- Create: `blog-frontend/vite.config.js`
- Create: `blog-frontend/index.html`
- Create: `blog-frontend/src/main.js`
- Create: `blog-frontend/src/App.vue`
- Create: `blog-frontend/src/styles/global.css`
- Create: `blog-frontend/src/styles/theme.css`

- [ ] **Step 1: Create project structure**

```powershell
$frontend = "D:\03PerProject\blog-frontend"
$dirs = @(
    "src\router", "src\api", "src\stores", "src\styles",
    "src\utils", "src\views\admin", "src\components\admin"
)
foreach ($d in $dirs) { New-Item -ItemType Directory -Path "$frontend\$d" -Force | Out-Null }
```

- [ ] **Step 2: Write `package.json`**

```json
{
  "name": "blog-frontend",
  "version": "0.1.0",
  "private": true,
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "vue": "^3.4.0",
    "vue-router": "^4.3.0",
    "pinia": "^2.1.0",
    "axios": "^1.6.0",
    "element-plus": "^2.5.0",
    "marked": "^11.0.0",
    "highlight.js": "^11.9.0",
    "@vueuse/core": "^10.7.0"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.0.0",
    "vite": "^5.0.0"
  }
}
```

- [ ] **Step 3: Write `vite.config.js`**

```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/uploads': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

- [ ] **Step 4: Write `index.html`**

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>个人博客</title>
</head>
<body>
  <div id="app"></div>
  <script type="module" src="/src/main.js"></script>
</body>
</html>
```

- [ ] **Step 5: Write `src/main.js`**

```javascript
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import './styles/global.css'
import './styles/theme.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(ElementPlus)
app.mount('#app')
```

- [ ] **Step 6: Write `src/App.vue`**

```vue
<template>
  <router-view />
</template>

<script setup>
import { useThemeStore } from './stores/theme'
import { onMounted } from 'vue'

const themeStore = useThemeStore()
onMounted(() => {
  themeStore.init()
})
</script>
```

- [ ] **Step 7: Write theme CSS**

`src/styles/global.css`:
```css
* { margin: 0; padding: 0; box-sizing: border-box; }
body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; }
a { text-decoration: none; color: var(--el-color-primary); }

/* Article content styles */
.article-content h1, .article-content h2, .article-content h3 { margin: 1.5em 0 0.5em; }
.article-content p { margin: 0.8em 0; line-height: 1.8; }
.article-content pre {
  background: var(--code-bg, #f5f5f5);
  padding: 1em;
  border-radius: 6px;
  overflow-x: auto;
  margin: 1em 0;
}
.article-content code {
  font-family: 'Fira Code', monospace;
  font-size: 0.9em;
}
.article-content img { max-width: 100%; border-radius: 8px; margin: 1em 0; }
.article-content blockquote {
  border-left: 4px solid var(--el-color-primary);
  padding-left: 1em;
  color: #666;
  margin: 1em 0;
}
```

`src/styles/theme.css`:
```css
:root {
  --bg-color: #ffffff;
  --text-color: #333333;
  --card-bg: #ffffff;
  --code-bg: #f5f5f5;
  --border-color: #e4e7ed;
}

[data-theme="dark"] {
  --bg-color: #1a1a2e;
  --text-color: #e0e0e0;
  --card-bg: #16213e;
  --code-bg: #0f3460;
  --border-color: #2a2a4a;
}

body {
  background-color: var(--bg-color);
  color: var(--text-color);
  transition: background-color 0.3s, color 0.3s;
}
```

- [ ] **Step 8: Install dependencies**

Run: `cd D:\03PerProject\blog-frontend && npm install`
Expected: node_modules created, no errors

---

### Task 16: Frontend — Router + API layer + Stores

**Files:**
- Create: `blog-frontend/src/router/index.js`
- Create: `blog-frontend/src/api/request.js`
- Create: `blog-frontend/src/api/auth.js`
- Create: `blog-frontend/src/api/article.js`
- Create: `blog-frontend/src/api/tag.js`
- Create: `blog-frontend/src/api/user.js`
- Create: `blog-frontend/src/api/image.js`
- Create: `blog-frontend/src/stores/auth.js`
- Create: `blog-frontend/src/stores/theme.js`
- Create: `blog-frontend/src/utils/index.js`

- [ ] **Step 1: Write `src/router/index.js`**

```javascript
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', name: 'Home', component: () => import('../views/Home.vue') },
  { path: '/article/:id', name: 'ArticleDetail', component: () => import('../views/ArticleDetail.vue') },
  { path: '/tag/:name', name: 'TagArticles', component: () => import('../views/TagArticles.vue') },
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('../views/Register.vue') },

  // Admin routes
  {
    path: '/admin',
    component: () => import('../components/admin/AdminLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', component: () => import('../views/admin/Dashboard.vue') },
      { path: 'articles', component: () => import('../views/admin/Articles.vue') },
      { path: 'articles/create', component: () => import('../views/admin/ArticleEdit.vue') },
      { path: 'articles/edit/:id', component: () => import('../views/admin/ArticleEdit.vue') },
      { path: 'tags', component: () => import('../views/admin/Tags.vue') },
      { path: 'images', component: () => import('../views/admin/Images.vue') },
      { path: 'users', component: () => import('../views/admin/Users.vue') },
      { path: 'profile', component: () => import('../views/admin/Profile.vue') },
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
```

- [ ] **Step 2: Write `src/api/request.js`**

```javascript
import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    ElMessage.error(error.response?.data?.message || error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request
```

- [ ] **Step 3: Write API modules**

`src/api/auth.js`:
```javascript
import request from './request'
export const login = data => request.post('/auth/login', data)
export const register = data => request.post('/auth/register', data)
export const getMe = () => request.get('/auth/me')
```

`src/api/article.js`:
```javascript
import request from './request'
export const getArticles = params => request.get('/articles', { params })
export const getArticle = id => request.get(`/articles/${id}`)
export const createArticle = data => request.post('/articles', data)
export const updateArticle = (id, data) => request.put(`/articles/${id}`, data)
export const deleteArticle = id => request.delete(`/articles/${id}`)
export const getAdminArticles = params => request.get('/admin/articles', { params })
```

`src/api/tag.js`:
```javascript
import request from './request'
export const getTags = () => request.get('/tags')
export const createTag = data => request.post('/admin/tags', data)
export const deleteTag = id => request.delete(`/admin/tags/${id}`)
```

`src/api/user.js`:
```javascript
import request from './request'
export const getUsers = params => request.get('/admin/users', { params })
export const getUser = id => request.get(`/admin/users/${id}`)
export const approveUser = id => request.put(`/admin/users/${id}/approve`)
export const disableUser = id => request.put(`/admin/users/${id}/disable`)
```

`src/api/image.js`:
```javascript
import request from './request'
export const uploadImage = file => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/admin/upload/image', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
export const deleteImage = id => request.delete(`/admin/upload/image/${id}`)
export const getImages = params => request.get('/admin/upload/images', { params })
```

- [ ] **Step 4: Write stores**

`src/stores/auth.js`:
```javascript
import { defineStore } from 'pinia'
import { getMe } from '../api/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    token: localStorage.getItem('token') || ''
  }),
  getters: {
    isLoggedIn: state => !!state.token,
    isAdmin: state => state.user?.role === 'admin',
    isActive: state => state.user?.status === 'active'
  },
  actions: {
    setToken(token) {
      this.token = token
      localStorage.setItem('token', token)
    },
    logout() {
      this.user = null
      this.token = ''
      localStorage.removeItem('token')
    },
    async fetchUser() {
      try {
        const res = await getMe()
        this.user = res.data
      } catch {
        this.logout()
      }
    }
  }
})
```

`src/stores/theme.js`:
```javascript
import { defineStore } from 'pinia'
import { usePreferredDark, useStorage } from '@vueuse/core'

export const useThemeStore = defineStore('theme', {
  state: () => ({
    isDark: false
  }),
  actions: {
    init() {
      const stored = localStorage.getItem('theme')
      if (stored) {
        this.isDark = stored === 'dark'
      } else {
        this.isDark = usePreferredDark().value
      }
      this.apply()
    },
    toggle() {
      this.isDark = !this.isDark
      localStorage.setItem('theme', this.isDark ? 'dark' : 'light')
      this.apply()
    },
    apply() {
      document.documentElement.setAttribute('data-theme', this.isDark ? 'dark' : 'light')
    }
  }
})
```

- [ ] **Step 5: Write `src/utils/index.js`**

```javascript
export function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return d.toLocaleDateString('zh-CN', {
    year: 'numeric', month: 'long', day: 'numeric'
  })
}

export function stripHtml(text) {
  const div = document.createElement('div')
  div.innerHTML = text || ''
  return div.textContent || div.innerText || ''
}

export function truncate(text, len = 100) {
  if (!text) return ''
  return text.length > len ? text.substring(0, len) + '...' : text
}
```

---

### Task 17: Frontend — Layout components (Header + Theme + Markdown)

**Files:**
- Create: `blog-frontend/src/components/AppHeader.vue`
- Create: `blog-frontend/src/components/ThemeToggle.vue`
- Create: `blog-frontend/src/components/ArticleCard.vue`
- Create: `blog-frontend/src/components/MarkdownRenderer.vue`
- Create: `blog-frontend/src/components/admin/AdminLayout.vue`

- [ ] **Step 1: Write `AppHeader.vue`**

```vue
<template>
  <el-header class="app-header">
    <div class="header-inner">
      <router-link to="/" class="logo">ZZY Blog</router-link>
      <div class="header-right">
        <ThemeToggle />
        <template v-if="authStore.isLoggedIn">
          <el-dropdown>
            <span class="user-info">
              {{ authStore.user?.nickname || authStore.user?.username }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push('/admin/dashboard')">后台管理</el-dropdown-item>
                <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button text @click="$router.push('/login')">登录</el-button>
          <el-button text @click="$router.push('/register')">注册</el-button>
        </template>
      </div>
    </div>
  </el-header>
</template>

<script setup>
import { useAuthStore } from '../stores/auth'
import { useRouter } from 'vue-router'
import ThemeToggle from './ThemeToggle.vue'
import { ArrowDown } from '@element-plus/icons-vue'

const authStore = useAuthStore()
const router = useRouter()

function logout() {
  authStore.logout()
  router.push('/')
}
</script>

<style scoped>
.app-header {
  border-bottom: 1px solid var(--border-color);
  background: var(--card-bg);
  position: sticky;
  top: 0;
  z-index: 100;
}
.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
}
.logo { font-size: 1.4em; font-weight: bold; color: var(--el-color-primary); }
.header-right { display: flex; align-items: center; gap: 12px; }
.user-info { cursor: pointer; }
</style>
```

- [ ] **Step 2: Write `ThemeToggle.vue`**

```vue
<template>
  <el-button text @click="themeStore.toggle()" :icon="themeStore.isDark ? Moon : Sunny" />
</template>

<script setup>
import { useThemeStore } from '../stores/theme'
import { Moon, Sunny } from '@element-plus/icons-vue'
const themeStore = useThemeStore()
</script>
```

- [ ] **Step 3: Write `ArticleCard.vue`**

```vue
<template>
  <el-card class="article-card" shadow="hover" @click="$router.push(`/article/${article.id}`)">
    <div class="card-body">
      <div class="card-content">
        <h3 class="card-title">{{ article.title }}</h3>
        <p class="card-summary">{{ article.summary || truncate(stripHtml(article.content), 120) }}</p>
        <div class="card-meta">
          <span class="meta-date">{{ formatDate(article.createdAt) }}</span>
          <span class="meta-author" v-if="article.authorName">{{ article.authorName }}</span>
          <span class="meta-views">阅读 {{ article.viewCount || 0 }}</span>
        </div>
        <div class="card-tags" v-if="article.tags?.length">
          <el-tag v-for="tag in article.tags" :key="tag.id" size="small"
            @click.stop="$router.push(`/tag/${tag.name}`)">{{ tag.name }}</el-tag>
        </div>
      </div>
      <div class="card-image" v-if="article.coverImage">
        <img :src="article.coverImage" :alt="article.title" />
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { formatDate, stripHtml, truncate } from '../utils'

defineProps({ article: { type: Object, required: true } })
</script>

<style scoped>
.article-card { margin-bottom: 16px; cursor: pointer; background: var(--card-bg); }
.card-body { display: flex; gap: 16px; }
.card-content { flex: 1; }
.card-title { font-size: 1.2em; margin-bottom: 8px; }
.card-summary { color: #666; font-size: 0.9em; line-height: 1.6; }
.card-meta { display: flex; gap: 12px; margin: 8px 0; font-size: 0.8em; color: #999; }
.card-tags { display: flex; gap: 6px; flex-wrap: wrap; }
.card-image { width: 180px; flex-shrink: 0; }
.card-image img { width: 100%; height: 120px; object-fit: cover; border-radius: 6px; }
</style>
```

- [ ] **Step 4: Write `MarkdownRenderer.vue`**

```vue
<template>
  <div class="article-content" v-html="rendered" />
</template>

<script setup>
import { computed } from 'vue'
import { marked } from 'marked'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'

marked.setOptions({
  highlight(code, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try { return hljs.highlight(code, { language: lang }).value }
      catch {} }
    return hljs.highlightAuto(code).value
  }
})

const props = defineProps({ content: { type: String, default: '' } })
const rendered = computed(() => marked(props.content || ''))
</script>
```

- [ ] **Step 5: Write `AdminLayout.vue`**

```vue
<template>
  <el-container style="min-height: 100vh">
    <el-aside width="220px" class="admin-sidebar">
      <div class="admin-logo">Blog Admin</div>
      <el-menu :router="true" :default-active="route.path" background-color="#304156"
        text-color="#fff" active-text-color="#409EFF">
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataAnalysis /></el-icon><span>仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/admin/articles">
          <el-icon><Document /></el-icon><span>文章管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/tags">
          <el-icon><PriceTag /></el-icon><span>标签管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/images">
          <el-icon><Picture /></el-icon><span>图片管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/users" v-if="authStore.isAdmin">
          <el-icon><User /></el-icon><span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/profile">
          <el-icon><Setting /></el-icon><span>个人资料</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="admin-header">
        <span>欢迎回来，{{ authStore.user?.nickname }}</span>
        <div>
          <el-button text @click="$router.push('/')">返回博客</el-button>
          <el-button text @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-main><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { DataAnalysis, Document, PriceTag, Picture, User, Setting } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

function logout() {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.admin-sidebar { background: #304156; }
.admin-logo { height: 60px; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 1.2em; font-weight: bold; }
.admin-header { display: flex; align-items: center; justify-content: space-between; background: #fff; border-bottom: 1px solid #eee; }
</style>
```

---

### Task 18: Frontend — Public views

**Files:**
- Create: `blog-frontend/src/views/Home.vue`
- Create: `blog-frontend/src/views/ArticleDetail.vue`
- Create: `blog-frontend/src/views/TagArticles.vue`
- Create: `blog-frontend/src/views/Login.vue`
- Create: `blog-frontend/src/views/Register.vue`

- [ ] **Step 1: Write `Home.vue`**

```vue
<template>
  <div class="layout">
    <AppHeader />
    <el-main class="main">
      <div class="container">
        <div class="sidebar">
          <el-card>
            <template #header><span>标签</span></template>
            <div class="tag-list">
              <el-tag v-for="tag in tags" :key="tag.id"
                :type="activeTag === tag.id ? '' : 'info'"
                style="cursor:pointer; margin: 4px"
                @click="filterByTag(tag)">
                {{ tag.name }}
              </el-tag>
            </div>
          </el-card>
        </div>
        <div class="content">
          <ArticleCard v-for="article in articles" :key="article.id" :article="article" />
          <div class="pagination" v-if="total > size">
            <el-pagination
              v-model:current-page="page"
              :total="total"
              :page-size="size"
              layout="prev, pager, next"
              @current-change="loadArticles" />
          </div>
          <el-empty v-if="!articles.length && !loading" description="暂无文章" />
        </div>
      </div>
    </el-main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getArticles } from '../api/article'
import { getTags } from '../api/tag'
import AppHeader from '../components/AppHeader.vue'
import ArticleCard from '../components/ArticleCard.vue'

const route = useRoute()
const router = useRouter()
const articles = ref([])
const tags = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const activeTag = ref(null)
const loading = ref(false)

onMounted(async () => {
  const res = await getTags()
  tags.value = res.data || []
  await loadArticles()
})

async function loadArticles() {
  loading.value = true
  const params = { page: page.value, size: size.value }
  if (activeTag.value) params.tagId = activeTag.value
  const res = await getArticles(params)
  articles.value = res.data || []
  total.value = res.total || 0
  loading.value = false
}

function filterByTag(tag) {
  if (activeTag.value === tag.id) {
    activeTag.value = null
  } else {
    activeTag.value = tag.id
  }
  page.value = 1
  loadArticles()
}
</script>

<style scoped>
.layout { min-height: 100vh; }
.main { max-width: 1200px; margin: 0 auto; padding: 20px; }
.container { display: flex; gap: 24px; }
.sidebar { width: 260px; flex-shrink: 0; }
.content { flex: 1; min-width: 0; }
.tag-list { display: flex; flex-wrap: wrap; }
.pagination { display: flex; justify-content: center; margin: 20px 0; }
</style>
```

- [ ] **Step 2: Write `ArticleDetail.vue`**

```vue
<template>
  <div class="layout">
    <AppHeader />
    <el-main class="main">
      <div class="article-container" v-if="article">
        <h1 class="article-title">{{ article.title }}</h1>
        <div class="article-meta">
          <span>{{ formatDate(article.createdAt) }}</span>
          <span v-if="article.authorName"> · {{ article.authorName }}</span>
          <span> · 阅读 {{ article.viewCount || 0 }}</span>
        </div>
        <div class="article-tags" v-if="article.tags?.length">
          <el-tag v-for="tag in article.tags" :key="tag.id" size="small">{{ tag.name }}</el-tag>
        </div>
        <MarkdownRenderer :content="article.content" />
      </div>
      <el-skeleton v-else :rows="10" animated />
    </el-main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getArticle } from '../api/article'
import { formatDate } from '../utils'
import AppHeader from '../components/AppHeader.vue'
import MarkdownRenderer from '../components/MarkdownRenderer.vue'

const route = useRoute()
const article = ref(null)

onMounted(async () => {
  const res = await getArticle(route.params.id)
  article.value = res.data
})
</script>

<style scoped>
.main { max-width: 800px; margin: 0 auto; padding: 20px; }
.article-title { font-size: 2em; margin-bottom: 12px; }
.article-meta { color: #999; font-size: 0.9em; margin-bottom: 16px; }
.article-tags { margin-bottom: 16px; display: flex; gap: 8px; }
</style>
```

- [ ] **Step 3: Write `TagArticles.vue`**

```vue
<template>
  <div class="layout">
    <AppHeader />
    <el-main class="main">
      <h2 style="margin-bottom: 20px">标签：{{ route.params.name }}</h2>
      <ArticleCard v-for="article in articles" :key="article.id" :article="article" />
      <el-empty v-if="!articles.length" description="该标签下暂无文章" />
    </el-main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getArticles } from '../api/article'
import { getTags } from '../api/tag'
import AppHeader from '../components/AppHeader.vue'
import ArticleCard from '../components/ArticleCard.vue'

const route = useRoute()
const articles = ref([])

onMounted(async () => {
  const tagsRes = await getTags()
  const tag = (tagsRes.data || []).find(t => t.name === route.params.name)
  if (tag) {
    const res = await getArticles({ tagId: tag.id, size: 100 })
    articles.value = res.data || []
  }
})
</script>

<style scoped>
.main { max-width: 800px; margin: 0 auto; padding: 20px; }
</style>
```

- [ ] **Step 4: Write `Login.vue`**

```vue
<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <h2>登录</h2>
      <el-form :model="form" :rules="rules" ref="formRef" @submit.prevent="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" style="width:100%">登录</el-button>
        </el-form-item>
      </el-form>
      <div class="auth-link">还没有账号？<router-link to="/register">立即注册</router-link></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '../api/auth'
import { useAuthStore } from '../stores/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const formRef = ref(null)
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => {})
  if (!valid) return
  loading.value = true
  try {
    const res = await login(form)
    authStore.setToken(res.data.token)
    await authStore.fetchUser()
    ElMessage.success('登录成功')
    router.push('/admin/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page { display: flex; justify-content: center; align-items: center; min-height: 100vh; background: var(--bg-color); }
.auth-card { width: 400px; }
h2 { text-align: center; margin-bottom: 20px; }
.auth-link { text-align: center; font-size: 0.9em; }
</style>
```

- [ ] **Step 5: Write `Register.vue`**

```vue
<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <h2>注册</h2>
      <el-form :model="form" :rules="rules" ref="formRef" @submit.prevent="handleRegister">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" show-password />
        </el-form-item>
        <el-form-item prop="nickname">
          <el-input v-model="form.nickname" placeholder="昵称（选填）" />
        </el-form-item>
        <el-tag type="warning" style="margin-bottom:12px; width:100%; justify-content:center">
          注册后需管理员审核方可登录
        </el-tag>
        <el-form-item>
          <el-button type="primary" @click="handleRegister" :loading="loading" style="width:100%">注册</el-button>
        </el-form-item>
      </el-form>
      <div class="auth-link">已有账号？<router-link to="/login">立即登录</router-link></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '../api/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const formRef = ref(null)
const form = reactive({ username: '', password: '', nickname: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '长度3-50', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码不少于6位', trigger: 'blur' }]
}

async function handleRegister() {
  const valid = await formRef.value.validate().catch(() => {})
  if (!valid) return
  loading.value = true
  try {
    await register(form)
    ElMessage.success('注册成功，等待管理员审核')
    router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page { display: flex; justify-content: center; align-items: center; min-height: 100vh; background: var(--bg-color); }
.auth-card { width: 400px; }
h2 { text-align: center; margin-bottom: 20px; }
.auth-link { text-align: center; font-size: 0.9em; }
</style>
```

---

### Task 19: Frontend — Admin views

**Files:**
- Create: `blog-frontend/src/views/admin/Dashboard.vue`
- Create: `blog-frontend/src/views/admin/Articles.vue`
- Create: `blog-frontend/src/views/admin/ArticleEdit.vue`
- Create: `blog-frontend/src/views/admin/Tags.vue`
- Create: `blog-frontend/src/views/admin/Images.vue`
- Create: `blog-frontend/src/views/admin/Users.vue`
- Create: `blog-frontend/src/views/admin/Profile.vue`

- [ ] **Step 1: Write `Dashboard.vue`**

```vue
<template>
  <div>
    <h2>仪表盘</h2>
    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="6">
        <el-card><div class="stat"><span>文章总数</span><h3>{{ stats.totalArticles }}</h3></div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card><div class="stat"><span>已发布</span><h3>{{ stats.publishedArticles }}</h3></div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card><div class="stat"><span>标签数</span><h3>{{ stats.totalTags }}</h3></div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card><div class="stat"><span>待审核用户</span><h3>{{ stats.pendingUsers }}</h3></div></el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getArticles } from '../../api/article'
import { getTags } from '../../api/tag'
import { getUsers } from '../../api/user'

const stats = ref({ totalArticles: 0, publishedArticles: 0, totalTags: 0, pendingUsers: 0 })

onMounted(async () => {
  const artRes = await getArticles({ size: 1 })
  stats.value.totalArticles = artRes.total || 0
  const tagRes = await getTags()
  stats.value.totalTags = (tagRes.data || []).length
  try {
    const userRes = await getUsers({ size: 1 })
    stats.value.pendingUsers = userRes.total || 0
  } catch {}
})
</script>

<style scoped>
.stat { text-align: center; }
.stat span { color: #999; font-size: 0.9em; }
.stat h3 { font-size: 2em; margin-top: 8px; }
</style>
```

- [ ] **Step 2: Write `Articles.vue`**

```vue
<template>
  <div>
    <div class="page-header">
      <h2>文章管理</h2>
      <el-button type="primary" @click="$router.push('/admin/articles/create')">写文章</el-button>
    </div>
    <el-table :data="articles" stripe style="margin-top:16px">
      <el-table-column prop="title" label="标题" min-width="200" />
      <el-table-column prop="createdBy" label="作者" width="120" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'published' ? 'success' : 'info'">
            {{ row.status === 'published' ? '已发布' : '草稿' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="viewCount" label="阅读" width="80" />
      <el-table-column prop="createdAt" label="时间" width="180">
        <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="$router.push(`/admin/articles/edit/${row.id}`)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-if="total > size" v-model:current-page="page" :total="total"
      :page-size="size" layout="prev, pager, next" @current-change="loadData" style="margin-top:16px" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAdminArticles, deleteArticle } from '../../api/article'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDate } from '../../utils'

const articles = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

onMounted(() => loadData())
async function loadData() {
  const res = await getAdminArticles({ page: page.value, size: size.value })
  articles.value = res.data || []
  total.value = res.total || 0
}
async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除该文章？', '提示')
  await deleteArticle(id)
  ElMessage.success('删除成功')
  loadData()
}
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
</style>
```

- [ ] **Step 3: Write `ArticleEdit.vue`**

```vue
<template>
  <div>
    <h2>{{ isEdit ? '编辑文章' : '写文章' }}</h2>
    <el-form :model="form" label-position="top" style="margin-top:16px">
      <el-form-item label="标题" :required="true">
        <el-input v-model="form.title" placeholder="文章标题" />
      </el-form-item>
      <el-form-item label="摘要">
        <el-input v-model="form.summary" type="textarea" :rows="2" placeholder="文章摘要（选填）" />
      </el-form-item>
      <el-form-item label="封面图">
        <ImageUploader v-model="form.coverImage" />
      </el-form-item>
      <el-form-item label="标签">
        <el-select v-model="form.tagIds" multiple placeholder="选择标签" style="width:100%">
          <el-option v-for="tag in tags" :key="tag.id" :label="tag.name" :value="tag.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="内容">
        <ArticleEditor v-model="form.content" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="save('draft')">保存草稿</el-button>
        <el-button type="success" @click="save('published')">发布</el-button>
        <el-button @click="$router.back()">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { createArticle, updateArticle, getArticle } from '../../api/article'
import { getTags } from '../../api/tag'
import { ElMessage } from 'element-plus'
import ArticleEditor from '../../components/admin/ArticleEditor.vue'
import ImageUploader from '../../components/admin/ImageUploader.vue'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.params.id)
const tags = ref([])
const form = reactive({
  title: '', content: '', summary: '', coverImage: '', status: 'draft', tagIds: []
})

onMounted(async () => {
  const tagRes = await getTags()
  tags.value = tagRes.data || []
  if (isEdit.value) {
    const res = await getArticle(route.params.id)
    const a = res.data
    form.title = a.title; form.content = a.content; form.summary = a.summary || ''
    form.coverImage = a.coverImage || ''
    form.tagIds = (a.tags || []).map(t => t.id)
  }
})

async function save(status) {
  form.status = status
  if (isEdit.value) {
    await updateArticle(route.params.id, form)
    ElMessage.success('更新成功')
  } else {
    await createArticle(form)
    ElMessage.success('创建成功')
  }
  router.push('/admin/articles')
}
</script>
```

- [ ] **Step 4: Write `Tags.vue`**

```vue
<template>
  <div>
    <div class="page-header">
      <h2>标签管理</h2>
      <div>
        <el-input v-model="newTagName" placeholder="标签名" style="width:200px; margin-right:8px" />
        <el-button type="primary" @click="handleCreate">新增</el-button>
      </div>
    </div>
    <el-table :data="tags" stripe style="margin-top:16px">
      <el-table-column prop="name" label="标签名" />
      <el-table-column prop="createdAt" label="创建时间">
        <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getTags, createTag, deleteTag } from '../../api/tag'
import { ElMessage } from 'element-plus'
import { formatDate } from '../../utils'

const tags = ref([])
const newTagName = ref('')

onMounted(async () => {
  const res = await getTags()
  tags.value = res.data || []
})

async function handleCreate() {
  if (!newTagName.value.trim()) return
  await createTag({ name: newTagName.value.trim() })
  ElMessage.success('创建成功')
  newTagName.value = ''
  const res = await getTags()
  tags.value = res.data || []
}

async function handleDelete(id) {
  await deleteTag(id)
  ElMessage.success('删除成功')
  tags.value = tags.value.filter(t => t.id !== id)
}
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
</style>
```

- [ ] **Step 5: Write `Images.vue`**

```vue
<template>
  <div>
    <div class="page-header">
      <h2>图片管理</h2>
      <ImageUploader v-model="uploadUrl" @success="loadData" />
    </div>
    <el-table :data="images" stripe style="margin-top:16px">
      <el-table-column label="预览" width="100">
        <template #default="{ row }">
          <el-image :src="row.url" style="width:60px; height:60px; object-fit:cover" />
        </template>
      </el-table-column>
      <el-table-column prop="originalName" label="文件名" />
      <el-table-column prop="size" label="大小" width="100">
        <template #default="{ row }">{{ (row.size / 1024).toFixed(1) }} KB</template>
      </el-table-column>
      <el-table-column prop="createdAt" label="上传时间" width="180">
        <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getImages, deleteImage } from '../../api/image'
import { ElMessage } from 'element-plus'
import { formatDate } from '../../utils'
import ImageUploader from '../../components/admin/ImageUploader.vue'

const images = ref([])
const uploadUrl = ref('')

onMounted(() => loadData())
async function loadData() {
  const res = await getImages({ size: 100 })
  images.value = res.data || []
}
async function handleDelete(id) {
  await deleteImage(id)
  ElMessage.success('删除成功')
  loadData()
}
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
</style>
```

- [ ] **Step 6: Write `Users.vue`**

```vue
<template>
  <div>
    <h2>用户管理</h2>
    <el-table :data="users" stripe style="margin-top:16px">
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="role" label="角色" width="100">
        <template #default="{ row }">
          <el-tag :type="row.role === 'admin' ? 'danger' : 'info'">{{ row.role }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'active' ? 'success' : row.status === 'pending' ? 'warning' : 'info'">
            {{ row.status === 'active' ? '已激活' : row.status === 'pending' ? '待审核' : '已禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button v-if="row.status === 'pending'" size="small" type="success"
            @click="handleApprove(row.id)">审核通过</el-button>
          <el-button v-if="row.status === 'active'" size="small" type="warning"
            @click="handleDisable(row.id)">禁用</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getUsers, approveUser, disableUser } from '../../api/user'
import { ElMessage } from 'element-plus'

const users = ref([])

onMounted(async () => {
  const res = await getUsers({ size: 100 })
  users.value = res.data || []
})

async function handleApprove(id) {
  await approveUser(id)
  ElMessage.success('已审核通过')
  const res = await getUsers({ size: 100 })
  users.value = res.data || []
}
async function handleDisable(id) {
  await disableUser(id)
  ElMessage.success('已禁用')
  const res = await getUsers({ size: 100 })
  users.value = res.data || []
}
</script>
```

- [ ] **Step 7: Write `Profile.vue`**

```vue
<template>
  <div>
    <h2>个人资料</h2>
    <el-descriptions :column="1" border style="margin-top:16px">
      <el-descriptions-item label="用户名">{{ user?.username }}</el-descriptions-item>
      <el-descriptions-item label="昵称">{{ user?.nickname }}</el-descriptions-item>
      <el-descriptions-item label="邮箱">{{ user?.email || '-' }}</el-descriptions-item>
      <el-descriptions-item label="角色">{{ user?.role }}</el-descriptions-item>
      <el-descriptions-item label="状态">
        <el-tag :type="user?.status === 'active' ? 'success' : 'warning'">{{ user?.status }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="注册时间">{{ formatDate(user?.createdAt) }}</el-descriptions-item>
    </el-descriptions>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useAuthStore } from '../../stores/auth'
import { formatDate } from '../../utils'

const authStore = useAuthStore()
const user = computed(() => authStore.user)
</script>
```

---

### Task 20: Frontend — Admin editor components

**Files:**
- Create: `blog-frontend/src/components/admin/ArticleEditor.vue`
- Create: `blog-frontend/src/components/admin/ImageUploader.vue`

- [ ] **Step 1: Write `ArticleEditor.vue`**

```vue
<template>
  <div class="editor-wrapper">
    <el-input
      v-model="content"
      type="textarea"
      :rows="20"
      placeholder="使用 Markdown 语法编写文章..."
      @input="emit"
      class="editor-textarea"
    />
    <div class="editor-preview">
      <div class="preview-header">预览</div>
      <MarkdownRenderer :content="content" />
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import MarkdownRenderer from '../MarkdownRenderer.vue'

const props = defineProps({ modelValue: { type: String, default: '' } })
const emit = defineEmits(['update:modelValue'])
const content = ref(props.modelValue)

watch(() => props.modelValue, v => content.value = v)
watch(content, v => emit('update:modelValue', v))
</script>

<style scoped>
.editor-wrapper { display: flex; gap: 16px; min-height: 500px; }
.editor-textarea, .editor-preview { flex: 1; }
.editor-textarea :deep(textarea) { font-family: 'Fira Code', monospace; font-size: 14px; line-height: 1.6; min-height: 500px; }
.preview-header { padding: 8px; background: #f5f5f5; border-radius: 4px 4px 0 0; font-size: 0.9em; color: #666; }
.editor-preview { border: 1px solid #dcdfe6; border-radius: 4px; padding: 12px; overflow-y: auto; max-height: 600px; }
</style>
```

- [ ] **Step 2: Write `ImageUploader.vue`**

```vue
<template>
  <div class="uploader">
    <el-upload
      :http-request="handleUpload"
      :show-file-list="false"
      accept="image/*"
      class="upload-btn"
    >
      <el-button type="primary">上传图片</el-button>
    </el-upload>
    <div v-if="modelValue" class="preview">
      <img :src="modelValue" style="max-width:200px; max-height:120px" />
      <el-button size="small" type="danger" @click="$emit('update:modelValue', '')" circle>×</el-button>
    </div>
  </div>
</template>

<script setup>
import { uploadImage } from '../../api/image'
import { ElMessage } from 'element-plus'

const props = defineProps({ modelValue: { type: String, default: '' } })
const emit = defineEmits(['update:modelValue', 'success'])

async function handleUpload(options) {
  try {
    const res = await uploadImage(options.file)
    emit('update:modelValue', res.data.url)
    emit('success')
    ElMessage.success('上传成功')
  } catch {
    ElMessage.error('上传失败')
  }
}
</script>

<style scoped>
.uploader { display: flex; align-items: center; gap: 12px; }
.preview { position: relative; }
.preview img { border-radius: 4px; }
.preview .el-button { position: absolute; top: -8px; right: -8px; }
</style>
```

---

### Task 21: Integration test — full stack verification

- [ ] **Step 1: Start backend**

Run: `cd D:\03PerProject\blog-backend && mvn spring-boot:run`
Expected: starts on port 8080, no errors

- [ ] **Step 2: Start frontend**

Run: `cd D:\03PerProject\blog-frontend && npm run dev`
Expected: starts on port 5173

- [ ] **Step 3: Test flow — register → approve → login → create article → view**

1. Open http://localhost:5173 → should see empty blog homepage
2. Register a new user → should redirect to login with success message
3. Login as admin (need to insert admin user directly into DB):
   ```sql
   INSERT INTO `user` (id, username, password, nickname, role, status, created_at, deleted)
   VALUES (1, 'admin', '$2a$10$...', 'Admin', 'admin', 'active', NOW(), 0);
   ```
   Generate BCrypt password for `admin123` using a BCrypt online tool or a small Java snippet.
4. Login as admin → should enter admin dashboard
5. Approve the registered user
6. Logout, login as user → should be able to create articles
7. Create article with tags → publish
8. View on homepage → article card should appear
9. Click article → should render Markdown with code highlighting
10. Test theme toggle → should switch light/dark

- [ ] **Step 4: Build frontend for production**

Run: `cd D:\03PerProject\blog-frontend && npm run build`
Expected: `dist/` folder created.

- [ ] **Step 5: Commit frontend**

```bash
cd D:\03PerProject\blog-frontend
git init
git add .
git commit -m "feat: initialize blog frontend with Vue 3 + Element Plus"
```

---

## Spec Coverage Check

| Spec Section | Covered By |
|---|---|
| Project structure | Task 1 (scaffolding) |
| Database design (6 tables, base fields) | Task 3 (SQL) + Task 4 (entities) |
| MyBatis-Plus config + base entity | Task 2 (common classes) |
| Spring Security + JWT | Task 7 (security config) |
| Auth: register, login, me | Task 8 (user service) + Task 11 (auth controller) |
| Article CRUD with tags | Task 9 (article service) + Task 12 (article controller) |
| Tag module | Task 10 (tag service) + Task 11 (tag controller) + Task 13 (admin tag) |
| Image upload | Task 10 (image service) + Task 13 (image controller) |
| Admin user management | Task 13 (admin user controller) |
| Role-based permissions | Task 7 (SecurityConfig + @PreAuthorize) |
| Unified response format | Task 2 (Result/PageResult) |
| Frontend: router, api layer, stores | Task 16 |
| Frontend: layouts + public views | Task 17 + Task 18 |
| Frontend: admin views | Task 19 + Task 20 |
| Tests | Task 14 |
| Light/dark theme | Task 16 (theme store) + Task 15 (CSS variables) |
| Markdown editor | Task 20 (ArticleEditor) + Task 17 (MarkdownRenderer) |
