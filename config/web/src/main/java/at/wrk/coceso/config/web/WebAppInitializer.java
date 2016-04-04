package at.wrk.coceso.config.web;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

public class WebAppInitializer implements WebApplicationInitializer {

  @Override
  public void onStartup(ServletContext sc) throws ServletException {
    AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
    rootContext.register(WebMvcConfigurer.class);

    FilterRegistration.Dynamic encodingFilter = sc.addFilter("encodingFilter", new CharacterEncodingFilter("UTF-8", true));
    encodingFilter.addMappingForUrlPatterns(null, true, "/*");
    encodingFilter.setAsyncSupported(true);

    FilterRegistration.Dynamic securityFilter = sc.addFilter("securityFilter", new DelegatingFilterProxy("springSecurityFilterChain"));
    securityFilter.addMappingForUrlPatterns(null, false, "/*");
    securityFilter.setAsyncSupported(true);

    ServletRegistration.Dynamic registration = sc.addServlet("dispatcher", new DispatcherServlet(rootContext));
    registration.setLoadOnStartup(1);
    registration.setAsyncSupported(true);
    registration.addMapping("/");
  }

}
