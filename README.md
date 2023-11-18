# PL_Project1
PL_Project1 - Simple Parser

## Build and Test Environment
- OS : Windows 11
- Compiler : Amazon Corretto 17
- Build System : Gradle 7.2

## How to Run
```bash
java -jar .\Main.jar .\testcase\example1.txt
```
or
```bash
java -jar .\Main.jar -v .\testcase\example1.txt
```

## How to Build
```bash
.\gradlew build
```

## Exception Handling
### Error
1. 선언되지 않은 `IDENT` 사용
2. `Statement`에서 `ASSIGNMENT_OP`가 나오지 않는 경우
3. `Statement`가 `IDENT`로 시작하지 않는 경우
4. `$`, `#`같은 예상치 못한 문자가 들어오는 경우
5. `Operator`가 없을 경우
6. `Operand`가 없을 경우
7. 파일이 공백일 경우

### Warning
1. `+`, `-`, `*`, `/` 연산자가 연속해서 나오는 경우 두번째 연산자부터 무시
2. `:=` 연산자가 연속해서 나오는 경우 두번째 연산자부터 무시
3. `:=` 연산자 대신 `:`, `=`가 나오는 경우 `:=`로 대체
4. 맨 마지막 `Statement`에서 `SEMI_COLON`이 나오면 제거
5. 괄호가 닫히지 않은 상태로 문장을 끝냈을 경우, 문장뒤에 나머지 괄호 삽입
