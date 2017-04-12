##jDialects (In Developing)
License: [LGPL 2.1](http://www.gnu.org/licenses/lgpl-2.1.html)  
  
jDialects is a small project which extract all Hibernate's dialects into a tiny jar, usually jDialects is used for build pagination SQL and DDL SQL to support mutiple databases, currently jDialects support 75 database dialects include SQLLite and Access and even text and XML files.  
  
jDialects is built for jSqlBox project, but it can be used for any project which use native SQL and need dialects features like pagination, if you are using JDBC/JDBCTemplate/DbUtils..., this tool may help.
  
jDialects is developed on Java8 but build on JDK1.6 to support more older Java versions.
  
Most source code of this project are generated by source code generator tool, and also it exported a Excel file "DatabaseDialects.xls", it's useful if you want compare database's differences in detail.

Use of jDialect: Download and put "jdialects-1.0.0.jar" in your project folder, or add below in pom.xml:
    <dependency>
       <groupId>com.github.drinkjava2</groupId>
       <artifactId>jdialects</artifactId>
       <version>1.0.0</version>
    </dependency>
   
2 usages of jDialects:
  1) Pagination    
     Dialect d=guessDialect(dataSource);
     String result=dialect.paginate(3, 10, "select * from user");//Page 3, each page has 10 items
     
     in MySQL5Dialect,    result is: "select * from users limit 20, 10"
     in Oracle8iDialect,  result is: "select * from ( select row_.*, rownum rownum_ from ( select * from users ) row_ ) where rownum_ <= 30 and rownum_ > 20"
     in Oracle12cDialect, result is: "select * from users offset 20 rows fetch next 10 rows only"
     in Sybase11Dialect, throw a DialectExcepiton with message: "Sybase11Dialect" does not support physical pagination
     ...
     
  2) Build DDL SQL:    	
       Dialect d=guessDialect(dataSource);
 		String ddlSql = "create table ddl_test("//
 				+ "f1 " + d.BIGINT() //
 				+ ",f3 " + d.BIT() //
 				+ ",f4 " + d.BLOB() //
 				+ ",f5 " + d.BOOLEAN() //
 				+ ",f6 " + d.CHAR() //
 				+ ")" + d.ENGINE();
     if MySql5Dialect, ddlSql will be "create table ddl_test(f1 bigint,f3 bit,f4 longblob,f5 bit,f6 char(1))engine=innoDB"
     in SQLServer2012Dialect, will get "create table ddl_test(f1 int8,f3 bool,f4 oid,f5 boolean,f6 char(1))"
  
 