package org.example.chatgpt.data.trigger.http;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.google.common.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;
import org.example.chatgpt.data.domain.auth.service.IAuthService;
import org.example.chatgpt.data.domain.order.model.entity.PayOrderEntity;
import org.example.chatgpt.data.domain.order.model.entity.ProductEntity;
import org.example.chatgpt.data.domain.order.model.entity.ShopCarEntity;
import org.example.chatgpt.data.domain.order.service.IOrderService;
import org.example.chatgpt.data.trigger.http.dto.SaleProductDTO;
import org.example.chatgpt.data.types.common.Constants;
import org.example.chatgpt.data.types.model.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/${app.config.api-version}/sale/")
public class SaleController {
    @Value("${alipay.alipay_public_key}")
    private String alipayPublicKey;
    @Resource
    private AlipayClient alipayClient;
    @Resource
    private IOrderService orderService;
    @Resource
    private IAuthService authService;
    @Resource
    private EventBus eventBus;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
    
    /**
     * @description 商品列表查询
     * 开始地址：http://localhost:8089/api/v1/sale/query_product_list
     * 测试地址：http://apix.natapp1.cc/api/v1/sale/query_product_list
     * <p>
     * curl -X GET \
     * -H "Authorization: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJveGZBOXc4LTI..." \
     * -H "Content-Type: application/x-www-form-urlencoded" \
     * http://localhost:8089/api/v1/sale/query_product_list
     */
    @RequestMapping(value = "query_product_list", method = RequestMethod.GET)
    public Response<List<SaleProductDTO>> queryProductList(@RequestHeader("Authorization") String token) {
        try {
            // 1. token 校验
            boolean success = authService.checkToken(token);
            if (!success) {
                return Response.<List<SaleProductDTO>>builder()
                        .code(Constants.ResponseCode.TOKEN_ERROR.getCode())
                        .info(Constants.ResponseCode.TOKEN_ERROR.getInfo())
                        .build();
            }
            // 2. 查询商品
            List<ProductEntity> productEntityList = orderService.queryProductList();
            log.info("商品查询 {}", JSON.toJSONString(productEntityList));
            List<SaleProductDTO> mallProductDTOS = new ArrayList<>();
            for (ProductEntity productEntity : productEntityList) {
                SaleProductDTO mallProductDTO = SaleProductDTO.builder()
                        .productId(productEntity.getProductId())
                        .productName(productEntity.getProductName())
                        .productDesc(productEntity.getProductDesc())
                        .price(productEntity.getPrice())
                        .quota(productEntity.getQuota())
                        .build();
                mallProductDTOS.add(mallProductDTO);
            }
            // 3. 返回结果
            return Response.<List<SaleProductDTO>>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(mallProductDTOS)
                    .build();
        } catch (Exception e) {
            log.error("商品查询失败", e);
            return Response.<List<SaleProductDTO>>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }
    /**
     * 用户商品下单
     * 开始地址：http://localhost:8089/api/v1/sale/create_pay_order?productId=
     * 测试地址：http://apix.natapp1.cc/api/v1/sale/create_pay_order
     * <p>
     * curl -X POST \
     * -H "Authorization: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJveGZBOXc4LTI..." \
     * -H "Content-Type: application/x-www-form-urlencoded" \
     * -d "productId=1001" \
     * http://localhost:8089/api/v1/sale/create_pay_order
     */
    @RequestMapping(value = "create_pay_order", method = RequestMethod.POST)
    public Response<String> createPayOrder(@RequestHeader("Authorization") String token, @RequestParam Integer productId) {
        try {
            // 1. token校验
            boolean success = authService.checkToken(token);
            if (!success) {
                return Response.<String>builder()
                        .code(Constants.ResponseCode.TOKEN_ERROR.getCode())
                        .info(Constants.ResponseCode.TOKEN_ERROR.getInfo())
                        .build();
            }
            // 2. 解析token
            String openid = authService.getOpenid(token);
            assert null != openid;
            log.info("用户商品下单，根据商品ID创建支付单开始 openid:{} productId:{}", openid, productId);
            ShopCarEntity shopCarEntity = ShopCarEntity.builder()
                    .openid(openid)
                    .productId(productId).build();
            PayOrderEntity payOrder = orderService.createOrder(shopCarEntity);
            log.info("用户商品下单，根据商品ID创建支付单完成 openid: {} productId: {} orderPay: {}", openid, productId, payOrder.toString());
            return Response.<String>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(payOrder.getPayUrl())
                    .build();
        } catch (Exception e) {
            log.error("用户商品下单，根据商品ID创建支付单失败", e);
            return Response.<String>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }
    /**
     * 支付回调
     * 开发地址：http:/localhost:8089/api/v1/sale/pay_notify
     * 测试地址：http://apix.natapp1.cc/api/v1/sale/pay_notify
     * 线上地址：https://你的域名/api/v1/sale/pay_notify
     */
    @RequestMapping(value = "pay_notify", method = RequestMethod.POST)
    public String payNotify(HttpServletRequest request) {
        try {
            log.info("支付回调，消息接收 {}", request.getParameter("trade_status"));
            if (request.getParameter("trade_status").equals("TRADE_SUCCESS")) {
                // 用户付款成功
                Map<String, String> params = new HashMap<>();
                Map<String, String[]> requestParams = request.getParameterMap();
                for (String name : requestParams.keySet()) {
                    params.put(name, request.getParameter(name));
                }
                String orderId = params.get("out_trade_no");
                String successTime = params.get("gmt_payment");
                // 支付宝交易号
                String transactionId = params.get("trade_no");
                // 元，精确到小数点后两位
                BigDecimal totalAmount = new BigDecimal(params.get("total_amount"));
                
                String charset = params.get("charset");
                String sign_type = params.get("sign_type");
                // 验签
                boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayPublicKey, charset, sign_type);
                if (signVerified) {
                    // 验签通过
                    log.info("支付成功 orderId:{} total:{} successTime: {}", orderId, totalAmount, successTime);
                    // 更新订单
                    boolean isSuccess = orderService.changeOrderPaySuccess(orderId, transactionId, totalAmount, dateFormat.parse(successTime));
                    // 更新订单成功
                    if (isSuccess) {
                        // 发布消息, 准备发货, 自己的业务场景可以使用MQ消息
                        eventBus.post(orderId);
                    }
                } else {
                    log.error("支付宝回调校验失败，参数可能被改变");
                    return "failure";
                }
            }
            // 回调需要返回一个"success"
            return "success";
        } catch (Exception e) {
            log.error("支付回调， 处理失败", e);
            return "failure";
        }
    }
}
