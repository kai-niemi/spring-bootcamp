 #!/bin/bash

 db_url="jdbc:postgresql://localhost:26257/bootcamp?sslmode=disable&allow_unsafe_internals=true"
 db_user=root
 db_password=cockroach

 spring_profile="default,verbose"

 if [ -n "$db_url" ]; then
 java -jar target/bootcamp-ch4-patterns.jar \
   --spring.datasource.url="${db_url}" \
   --spring.datasource.username=${db_user} \
   --spring.datasource.password=${db_password} \
   --spring.profiles.active="${spring_profile}" \
   $*
 else
 java -jar target/bootcamp-ch4-patterns.jar \
    --spring.profiles.active="${spring_profile}" \
    $*
 fi
