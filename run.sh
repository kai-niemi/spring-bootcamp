#!/bin/bash

if [ "$(whoami)" == "root" ]; then
    echo -e "[ FAIL ] Do NOT run as root!"
    exit 1
fi

fn_run_test() {
  PS3='Please select test class: '

  unset options i
  while IFS= read -r -d $'\0' f; do
    options[i++]="$f"
  done < <(find ${rootdir}/src/test/java -name '*Test.java'  -print0 )

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

  cd ${rootdir}

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
}