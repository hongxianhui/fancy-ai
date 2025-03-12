# Fancy AI [AI大模型工具]

[![Java Version](https://img.shields.io/badge/Java-17%2B-orange)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-blue)](https://opensource.org/licenses/MIT)
[![GitHub Stars](https://img.shields.io/github/stars/yourusername/shortvideogenerator?style=social)](https://github.com/yourusername/shortvideogenerator)

Fancy AI是一款基于开源大模型生成的工具集，可供开发者免费在线使用或本地部署和使用

## 🎥 在线演示
立即体验生成效果：  
👉 [在线演示地址](http://www.fancy-ai.cn)

## 🌟 功能特性
- 已集成deepseek满血版、通义千问等语言大模型
- 封装了阿里百练大模型平台的大部分API接口，包括文本对话，图片生成，图片理解，语音合成、视频生成等功能，开箱即用
- 集成了云端知识库，工具调用（Function Call），MCP(Message Context Protocol)等功能，可下载源码用来学习和调试
- 内置了一个幻灯片风格的短视频生成工具，输入一段提示词就可以一键生成带解说、背景音乐和字幕的灯片风格的短视频，另外还提供了API供使用者免费调用

## 🛠️ 技术栈
- ==&zwnj;**核心语言**&zwnj;==: Java 17
- ==&zwnj;**技术框架**&zwnj;==: Spring AI 1.0.0-M5.1
- ==&zwnj;**多媒体处理**&zwnj;==: JavaCV 1.5.11
- ==&zwnj;**依赖管理**&zwnj;==: Maven 3.9

## 🚀 快速开始

### 环境要求
- JDK 17+
- Maven 3.9+
- 可选：SQLLite+UV工具箱（演示MCP功能时使用）

### 安装步骤
```bash
# 克隆仓库
git clone https://github.com/hongxianhui/fancy-ai.git

# 构建项目
mvn clean package

# 启动
java -Djava.awt.headless=true -jar /opt/chat-1.0.0-SNAPSHOT.jar --ai.api-key.public={apikey} --ai.tempfile.folder=/opt/files/ --ai.mcp.database-path=/opt/db/test.db --ai.mcp.command-path=/root/.local/bin/uvx
```
### 说明
- ai.api-key.pulibc值为阿里百练大模型的apikey，请替换为实际的apikey，可自行云阿里云平台免费申请。
- ai.tempfile.folder值为临时文件存储路径，请替换为实际的临时文件存储路径。
- ai.mcp.database-path值为MCP数据库路径（不使用MCP功能可不填），请替换为实际的SQLite数据库路径。
- ai.mcp.command-path值为NV工具命令路径（不使用MCP功能可不填），请替换为实际的路径。

## 📚 API 详细介绍

# 生成剪贴画风格短视频
POST http://fancy-ai.cn/api/v1/clip

### 请求体
```json
{
    "token": "www.fancy-ai.cn",
    "bgm": "http://fancy-ai.cn/download?fileName=/test/bgm.wav",
    "voice": 1,
    "images": [
        "http://fancy-ai.cn/download?fileName=/test/1.png",
        "http://fancy-ai.cn/download?fileName=/test/2.png",
        "http://fancy-ai.cn/download?fileName=/test/3.png"
    ],
    "text": [
        "第一张图片的解说词",
        "第二张图片的解说词",
        "第三张图片的解说词"
    ]
}
```
### 响应体
```json
{
    "success": true,
    "data": "http://fancy-ai.cn/download?fileName=/flow/clipShow-1741764782751.mp4"
}
```
### 说明
- token参数为网站token，必填，值为www.fancy-ai.cn（限时免费试用）。
- images为图片集，最多可上传10张图片，每张图片的尺寸要求为720*1280，且图片格式为PNG或JPG。
- text为解说词，数量与图片数据要保持一致，每张图片对应解说词最多30个字。
- bgm参数为背景音乐地址，可不填，不填的话使用默认背景音乐，如果自定义，要求PCM编码WAV格式，4800Hz，单声道，16位。
- voice参数为朗读解说词的音色，可不填，0为男声，1为女声，默认男声