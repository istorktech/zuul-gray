package com.demo.zuul.config;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import io.jmnarloch.spring.cloud.ribbon.support.RibbonFilterContextHolder;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.*;

/**
 * 灰度发布 Filter
 */
public class GrayFilter extends ZuulFilter {
    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return PRE_DECORATION_FILTER_ORDER - 1;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext currentContext = RequestContext.getCurrentContext();
        return !currentContext.containsKey(FORWARD_TO_KEY) && !currentContext.containsKey(SERVICE_ID_KEY);
    }

    @Override
    public Object run() throws ZuulException {
        HttpServletRequest request = RequestContext.getCurrentContext().getRequest();
        String headerGrayMark = request.getHeader("header_gray_mark");
        String hostGrayMark = request.getParameter("host_gray_mark");
        // 是否灰度生效
        boolean grayEnable = !StringUtils.isEmpty(headerGrayMark) && "enable".equals(headerGrayMark);
        grayEnable = grayEnable || !StringUtils.isEmpty(hostGrayMark) && "enable".equals(hostGrayMark);

        if (grayEnable) {
            // 标识路由选择 eureka.instancemetadataMap.host-mark=gray 灰度服务集群
            RibbonFilterContextHolder.getCurrentContext().add("host-mark", "gray");
        } else {
            // 标识路由选择 eureka.instancemetadataMap.host-mark=running 正式服务集群
            RibbonFilterContextHolder.getCurrentContext().add("host-mark", "running");
        }
        return null;
    }
}
