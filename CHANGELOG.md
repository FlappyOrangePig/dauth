# Changelog

该项目的所有显著更改都将记录在该文件中。

格式基于[Keep a Changelog](https://keepachangelog.com/en/1.0.0/)，
并且该项目遵循[Semantic Versioning](https://semver.org/spec/v2.0.0.html)。

## [Unreleased]

## [0.0.18] - 2023-08-18

### Added

- 创建钱包速度优化

### Fixed

- 解决DAuthResult#isSuccess错误的问题

### Changed

- 移除部分测试接口
- 统计上报优化

## [0.0.17] - 2023-08-16

### Added

- 支持保存多账号钱包，退出登录不再删除钱包
- 首次创建钱包速度优化
- 耗时日志增加统计上报

### Fixed

- 解决twitter登录可能出现证书错误的问题
- 解决获取设备号错误的问题

### Changed

- 密钥从SharedPreference迁移到文件（由于未发布，需要重装后测试）

[unreleased]: https://github.com/FlappyOrangePig/dauth/tree/master
[0.0.18]: https://github.com/FlappyOrangePig/dauth/releases/tag/0.0.18
[0.0.17]: https://github.com/FlappyOrangePig/dauth/releases/tag/0.0.17