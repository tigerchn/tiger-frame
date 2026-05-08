package com.frame.websocket.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class WebSocketServerConfig extends ServerEndpointConfig.Configurator {

    /**
     * 跨域校验（安全、不空指针）
     */
    @Override
    public boolean checkOrigin(String originHeaderValue) {
        try {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
                HttpServletRequest request = servletRequestAttributes.getRequest();
                log.debug("WebSocket 握手来源: {}, 请求IP: {}", originHeaderValue, request.getRemoteAddr());
            }

            // 企业内部使用，放行；如需严格跨域，可在这里配置白名单
            return true;
        } catch (Exception e) {
            log.warn("WebSocket 跨域校验异常", e);
            return true;
        }
    }

    /**
     * 握手时获取参数（如 erp），存入 userProperties
     */
    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        try {
            Map<String, List<String>> parameterMap = request.getParameterMap();
            if (CollectionUtils.isEmpty(parameterMap)) {
                log.debug("WebSocket 握手参数为空");
                return;
            }

            // 安全获取 erp
            Optional.ofNullable(parameterMap.get("erp"))
                    .filter(erpList -> !erpList.isEmpty())
                    .ifPresent(erpList -> {
                        String erp = erpList.getFirst();
                        sec.getUserProperties().put("erp", erp);
                        log.debug("WebSocket 握手成功，erp: {}", erp);
                    });
        } catch (Exception e) {
            log.error("WebSocket 握手参数处理异常", e);
        }
    }
}