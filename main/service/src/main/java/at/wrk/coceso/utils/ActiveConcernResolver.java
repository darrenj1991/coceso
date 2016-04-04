package at.wrk.coceso.utils;

import at.wrk.coceso.entity.Concern;
import at.wrk.coceso.service.ConcernService;
import at.wrk.coceso.exceptions.ConcernException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.util.WebUtils;

@Component
public class ActiveConcernResolver implements HandlerMethodArgumentResolver {

  @Autowired
  private ConcernService concernService;

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return ((parameter.getParameterAnnotation(ActiveConcern.class) != null)
        && (parameter.getParameterType() == Concern.class
        || parameter.getParameterType() == Integer.class
        || parameter.getParameterType() == int.class));
  }

  @Override
  public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws ConcernException {
    boolean required = parameter.getParameterAnnotation(ActiveConcern.class).required();

    HttpServletRequest servletRequest = (HttpServletRequest) webRequest.getNativeRequest();
    Cookie cookie = WebUtils.getCookie(servletRequest, "concern");
    if (cookie == null) {
      if (required) {
        throw new ConcernException("Active concern cookie does not exist");
      }
      return null;
    }

    int concernId;
    try {
      concernId = Integer.parseInt(cookie.getValue());
    } catch (NumberFormatException e) {
      if (required) {
        throw new ConcernException("Could not parse active concern cookie");
      }
      return null;
    }

    if (concernId <= 0) {
      if (required) {
        throw new ConcernException("ConcernId is invalid");
      }
      return null;
    }

    Concern concern = concernService.getById(concernId);
    if (concern == null || concern.isClosed()) {
      if (required) {
        throw new ConcernException("Concern does not exist or is closed");
      }
      return null;
    }

    if (parameter.getParameterType() == Concern.class) {
      return concern;
    }
    return concern.getId();
  }

}
