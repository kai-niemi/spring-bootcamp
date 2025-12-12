#!/bin/bash

db_url="jdbc:postgresql://localhost:26257/bootcamp?sslmode=disable"
db_user=root
db_password=cockroach

spring_profile="default,verbose"
#spring_profile="default,verbose,rc"

echo ""
echo "Note: You need to add the 'rc' spring profile for ReadCommittedIsolationTest to succeed."
echo "Current profiles are: $spring_profile"
echo ""

####################################
# Do not edit past this line
####################################

PS3='Please select test class: '

unset options i
while IFS= read -r -d $'\0' f; do
  options[i++]="$f"
done < <(find src/test/java -name '*Test.java'  -print0 )

select opt in "${options[@]}" "Quit"; do
  case $opt in
    *.java)
      echo "Test class $opt selected"
      break
      ;;
    "Quit")
      exit 0
      ;;
    *)
      echo "Try again!"
      ;;
  esac
done

testClass=$(echo $opt --| awk -F'/' '{print $NF}' | sed 's/\.[^.]*$//')

if [ -n "$db_url" ]; then
../mvnw -DskipTests=false -Dgroups=integration-test -Dtest=$testClass \
  -Dspring.datasource.url="${db_url}" \
  -Dspring.datasource.username=${db_user} \
  -Dspring.datasource.password=${db_password} \
  -Dspring.profiles.active="${spring_profile}" \
  test
else
../mvnw -DskipTests=false -Dgroups=integration-test -Dtest=$testClass \
  -Dspring.profiles.active="${spring_profile}" \
  test
fi
