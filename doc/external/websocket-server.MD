签名服务，协议以json格式进行交互。
签名服务地址：
```
ws://api.infras.online/mpc/sign
```

## 一、建立websocket连接
其中后端的鉴权，签名请求都在握手过程中完成。包括两个自定义的http header。需要在握手时带上。
```
const string WebSocketTokenHeader = "Sec-WebSocket-Infras-Token";
const string MpcRequestHeader = "Infras-Mpc-Request";
```

### 1、请求签名接口
```
ws://api.infras.online/mpc/sign
```
通过http头Infras-Mpc-Request，进行请求签名。指定协议和参与者id。
id必须时签名私钥分片中id，不然无法通过校验。

示例：
```
{"signtype":"gg18","src":"co_signer0"}
```

定义:
```
typedef struct MpcSignRequest
{
    std::string signtype_; // gg18,gg20,cmp 
    std::string src_;   // party id in a mpc session, it shuold be unique
    bool ToJson(std::string &str) const
    {
        str.clear();
        neb::CJsonObject oJson;
        oJson.Add("src", src_);
        oJson.Add("signtype", signtype_);
        str = oJson.ToString();
        return true;
    };

    bool FromJson(const std::string &str)
    {
        neb::CJsonObject oJson;
        if (oJson.Parse(str))
        {
            oJson.Get("src", src_);
            oJson.Get("signtype", signtype_);
            return true;
        }
        return false;
    };
} MpcSignRequest;
```
### 2、身份校验
通过http头Sec-WebSocket-Infras-Token的信息来校验。
是dauth授权,jwt格式的IDToken。里面包含了用户的ID信息，也是后端保留的私钥的索引key。


## 二、签名过程，消息交换
签名过程中round消息交互接口，发送标准的mpc签名消息
示例：
```
{"src":"dauth0","bc_msg":"bc_msg","p2p_msg":"p2p_msg"}
```
定义：
```
// MpcMsg is the message format for mpc-node to mpc-node round communication
typedef struct MpcMsg
{
    std::string src_;     // party id in a mpc session, it shuold be unique
    std::string bc_msg_;  // broadcast message
    std::string p2p_msg_; // p2p message
    bool ToJson(std::string &str) const
    {
        str.clear();
        neb::CJsonObject oJson;
        oJson.Add("src", src_);
        oJson.Add("bc_msg", bc_msg_);
        oJson.Add("p2p_msg", p2p_msg_);
        str = oJson.ToString();
        return true;
    };

    bool FromJson(const std::string &str)
    {
        neb::CJsonObject oJson;
        if (oJson.Parse(str))
        {
            oJson.Get("src", src_);
            oJson.Get("bc_msg", bc_msg_);
            oJson.Get("p2p_msg", p2p_msg_);
            return true;
        }
        return false;
    };
} Msg;
```