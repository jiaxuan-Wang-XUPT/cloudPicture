# 🖼️ 智能协同云图库（Intelligent collaborative cloud image library）

[![Java](https://img.shields.io/badge/Java-17-blue.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Redis](https://img.shields.io/badge/Redis-6.2-red.svg)](https://redis.io/)
[![MyBatis Plus](https://img.shields.io/badge/MyBatis%20Plus-3.5-orange.svg)](https://baomidou.com/)
[![COS](https://img.shields.io/badge/腾讯云-COS-lightgrey.svg)](https://cloud.tencent.com/product/cos)
[![WebSocket](https://img.shields.io/badge/WebSocket-实时通信-purple.svg)](https://developer.mozilla.org/en-US/docs/Web/API/WebSockets_API)

## 🚀 项目简介

本项目基于 **Spring Boot + Redis + MyBatis Plus + COS 存储 + AI + WebSocket** 构建，实现了**公共图库、私有图库、团队共享图库**三大模块，支持多角色的图片管理与协同需求。  
平台不仅提供**分布式权限控制、图片存储与解析、多级缓存优化、实时协作与高吞吐消息处理**，还结合 **AI 能力**提升图片管理的智能化水平。

1. **所有用户**都可以在平台公开上传和检索图片素材，快速找到需要的图片。可用作表情包网站、设计素材网站、壁纸网站等。  
2. **管理员**可以上传、审核和管理图片，并对系统内的图片进行分析。  
3. 对于**个人用户**，可将图片上传至私有空间进行批量管理、检索、编辑和分析，用作个人网盘、个人相册、作品集等。  
4. 对于**企业**，可开通团队空间并邀请成员，共享图片并实时协同编辑图片，提高团队协作效率。可用于提供商业服务，如企业活动相册、企业内部素材库等。

## ✨ 核心功能（按角色）

### 🌍 公共图库（所有用户）
- 无需登录即可浏览、搜索、下载公开图片    
- 用户可自由上传图片/url上传至公共池（可选审核）  
- 典型应用：表情包网站、设计素材平台、壁纸站
![show](https://github.com/user-attachments/assets/c9129dae-d4fa-4410-a769-81b40254761e)


### 🛡️ 管理员后台
- 图片审核与管理（单张/批量创建图片、拒绝、删除、编辑）
- 用户信息的记录和删除
- 支持批量抓取图片
  <img width="1913" height="942" alt="image" src="https://github.com/user-attachments/assets/da85e33f-55fe-4b0c-a87d-4c2e5d36fa00" />
  <img width="1914" height="950" alt="image" src="https://github.com/user-attachments/assets/b3676914-312b-4cb2-8c89-d4d9c9efec8d" />


### 👤 个人空间
- 私有云盘：上传/移动/删除  
- 智能检索：文件名  +  分类标签 + 日期段 + 格式
- 图片编辑：裁剪、旋转、AI扩图 
<img width="1913" height="946" alt="image" src="https://github.com/user-attachments/assets/b140de5a-56f0-449e-ace2-27fd67be8470" />
<img width="1912" height="943" alt="image" src="https://github.com/user-attachments/assets/47e20248-28ef-4d9c-8cc0-ee808f22d606" />
<img width="1911" height="946" alt="image" src="https://github.com/user-attachments/assets/ccfa095a-bebf-4836-a421-d3841cd6dadd" />


### 👥 团队协作空间（企业/团队）
- 创建团队，邀请成员，分配角色（管理员/编辑/只读）  
- 实时协同：WebSocket 推送成员动作 + Disputor排队  
- 批量编辑：批量对图片进行统一编辑
<img width="1847" height="880" alt="image" src="https://github.com/user-attachments/assets/7f46e2e6-c415-43fa-a67d-2d92c5acfb89" />

## 🛠️ 技术栈

| 分类 | 技术 |
|------|------|
| 后端框架 | Spring Boot 2.7.x,  JWT |
| ORM | MyBatis Plus 3.5 |
| 缓存 | Redis（分布式锁+二级缓存）+ Caffeine（本地缓存） |
| 数据库 | MySQL 8.0 |
| 对象存储 | 腾讯云 COS |
| AI 服务 | 阿里百炼智能图像扩图 |
| 实时通信 | WebSocket + Spring Messaging + Disputor |
| 图片处理 | Thumbnails + Cropper.js |

## 🚀 快速开始

### 环境要求
- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.2+
