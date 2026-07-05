# DAM-HUB 企业图素中台

企业级图片素材平台，提供图片素材的上传、管理、检索和团队协作能力，帮助企业统一管理品牌视觉素材。

## 核心功能

- **图片管理**：上传、编辑、删除、批量导入，支持按颜色和关键词搜索
- **空间管理**：团队协作空间，支持成员管理和权限控制（管理员/编辑者/查看者）
- **空间分析**：分类统计、标签统计、存储用量、用户活跃度等多维度数据分析
- **用户系统**：注册登录、角色权限（普通用户/管理员）、VIP 会员体系
- **图片审核**：管理员审核机制，保障内容质量
- **AI 扩图**：基于阿里云 AI 的智能图片扩展

## 技术栈

### 后端

| 技术 | 说明 |
|------|------|
| Spring Boot 2.7 | 应用框架 |
| MyBatis-Plus | ORM 框架 |
| MySQL | 关系数据库 |
| Redis | 缓存 & Session 存储 |
| Sa-Token | 权限认证 |
| ShardingSphere | 数据库分表 |
| 腾讯云 COS | 对象存储 |
| Knife4j | API 文档 |

### 前端

| 技术 | 说明 |
|------|------|
| Vue 3 | 前端框架 |
| TypeScript | 类型系统 |
| Vite | 构建工具 |
| Element Plus | UI 组件库 |
| Pinia | 状态管理 |
| ECharts | 数据可视化 |
| Axios | HTTP 客户端 |

## 快速启动

### 环境要求

- JDK 8+
- Node.js 18+
- MySQL 5.7+
- Redis

### 后端

```bash
# 克隆项目
git clone https://github.com/konlue/dam-hub.git

# 创建数据库
mysql -u root -p -e "CREATE DATABASE dam_picture DEFAULT CHARACTER SET utf8mb4;"

# 修改配置
# 编辑 dam-hub-backend/src/main/resources/application-dev.yml
# 配置数据库连接、Redis、腾讯云 COS 等

# 启动
cd dam-hub-backend
mvn spring-boot:run
```

### 前端

```bash
cd dam-hub-frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build
```

访问 http://localhost:5173

## 项目结构

```
dam-hub/
├── dam-hub-backend/                # 后端 Spring Boot 项目
│   ├── controller/                 # 接口层
│   ├── service/                    # 业务层
│   ├── mapper/                     # 数据访问层
│   ├── model/                      # 实体 & DTO & VO
│   ├── manager/                    # 第三方服务封装（COS、AI、鉴权）
│   ├── config/                     # 配置类
│   └── resources/
│       └── application.yml         # 应用配置
│
└── dam-hub-frontend/               # 前端 Vue 3 项目
    ├── src/
    │   ├── api/                    # 接口调用（自动生成）
    │   ├── components/             # 公共组件
    │   ├── pages/                  # 页面组件
    │   ├── stores/                 # Pinia 状态管理
    │   ├── router/                 # 路由配置
    │   └── layouts/                # 布局组件
    └── package.json
```

## 接口文档

启动后端后访问：http://localhost:8123/api/doc.html

## 许可证

本项目仅供交流学习使用。