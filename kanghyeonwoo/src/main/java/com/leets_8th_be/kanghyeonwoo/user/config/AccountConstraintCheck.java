package com.leets_8th_be.kanghyeonwoo.user.config;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
@Configuration
public class AccountConstraintCheck {
 @Bean ApplicationRunner verifyAccountUniqueness(JdbcTemplate jdbc) {
  return args -> {
   for(String expression:java.util.List.of("lower(trim(email))","trim(username)")) {
    var conflicts=jdbc.queryForList("select "+expression+" as identity_value, count(*) as total from users group by "+expression+" having count(*) > 1");
    if(!conflicts.isEmpty()) throw new IllegalStateException("기존 사용자 중복 데이터를 먼저 정리해 주세요. docs/auth-security.md의 사전 점검 SQL을 참고하세요.");
   }
   // Existing installations must have both named constraints; CREATE UNIQUE INDEX also fails closed on conflicts.
   jdbc.execute((org.springframework.jdbc.core.ConnectionCallback<Void>) connection -> {
    var names=new java.util.HashSet<String>();
    var metadata=connection.getMetaData();
    for(String table:java.util.List.of("users","USERS")) {
     try(var indexes=metadata.getIndexInfo(connection.getCatalog(),null,table,true,false)) {
      while(indexes.next()) {var name=indexes.getString("INDEX_NAME");if(name!=null) names.add(name.toLowerCase(java.util.Locale.ROOT));}
     }
    }
    for(var constraint:java.util.Map.of("uk_user_email","email","uk_user_nickname","username").entrySet()) {
     boolean exists=names.stream().anyMatch(name->name.equals(constraint.getKey()) || name.startsWith(constraint.getKey()+"_index_"));
     if(!exists) try(var statement=connection.createStatement()) {statement.execute("CREATE UNIQUE INDEX "+constraint.getKey()+" ON users("+constraint.getValue()+")");}
    }
    return null;
   });
  };
 }
}
