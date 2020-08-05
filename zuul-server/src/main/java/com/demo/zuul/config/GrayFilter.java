package com.demo.zuul.config;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import io.jmnarloch.spring.cloud.ribbon.support.RibbonFilterContextHolder;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.*;

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
        boolean grayEnable = !StringUtils.isEmpty(headerGrayMark) && "enable".equals(headerGrayMark);
        grayEnable = grayEnable || !StringUtils.isEmpty(hostGrayMark) && "enable".equals(hostGrayMark);

        if (grayEnable) {
            //
            RibbonFilterContextHolder.getCurrentContext().add("host-mark", "gray");
        } else {
            RibbonFilterContextHolder.getCurrentContext().add("host-mark", "running");
        }
        return null;
    }
}
