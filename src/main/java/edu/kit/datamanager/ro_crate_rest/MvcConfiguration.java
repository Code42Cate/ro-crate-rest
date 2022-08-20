package edu.kit.datamanager.ro_crate_rest;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import edu.kit.datamanager.ro_crate_rest.interceptors.CrateInterceptor;

@EnableWebMvc
@Configuration
public class MvcConfiguration implements WebMvcConfigurer {

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new CrateInterceptor());
  }
}