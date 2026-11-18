# 环境配置

此仓库包含 ShopLab 当前代码快照，包括手机号、邮箱验证码注册功能。
源版本为 `d3f1248`，上传快照未包含原提交历史。

启动后端前设置以下环境变量，实际值不要提交到 Git：

- `SHOP_DB_PASSWORD`：MySQL 密码。
- `SHOP_JWT_SECRET`：独立生成的随机 JWT 密钥。
- `SHOP_REDIS_PASSWORD`：Redis 设置了密码时提供，未设置时可留空。
- `REGISTRATION_DELIVERY_URL`：提供真实验证码投递的 HTTPS 网关地址。
- `REGISTRATION_DELIVERY_TOKEN`：该网关的访问令牌。

默认连接本机 MySQL 3307 端口的 `shop_db`，以及 Redis 6379 端口。
数据库结构及演示数据使用 `backend/src/main/resources/db/init.sql` 初始化。
上传快照不包含原数据库导出文件 `shop_db.sql`。
若这些凭据曾被公开，应更换数据库密码和 JWT 密钥；更换 JWT 密钥会使旧会话失效。

`registration-test` 仅用于本机隔离测试，需要单独设置
`REGISTRATION_TEST_DATABASE_URL` 和 `REGISTRATION_TEST_KEY`。真实验证码投递
验证使用默认配置。

仓库初始化代码包含演示管理员账号。部署到真实服务前应更换默认管理员
凭据，并处理开发用的管理员自动初始化逻辑。
