# ZZY Blog

个人博客项目，包含后端、前端和项目文档。

## 目录

- `blog-backend/`：Spring Boot 后端
- `blog-frontend/`：Vue 前端
- `docs/`：设计文档、执行计划和协作说明

## 启动

后端：

```powershell
cd blog-backend
mvn.cmd spring-boot:run
```

前端：

```powershell
cd blog-frontend
npm.cmd run dev -- --host 127.0.0.1
```

## 协作注意事项

- 新对话先看 `README.md` 和 `docs/协作说明.md`。
- `git commit` 提交说明统一使用中文。
- 文件名尽量使用中文，尤其是文档和说明类文件。
- 不要擅自回滚未确认的工作区改动。
- 数据库结构变更优先使用增量脚本，避免直接重建数据库。
- 提交前先确认当前任务范围，不要把无关文件混进同一次提交。
