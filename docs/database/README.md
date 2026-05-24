# Catch-CBNU Database Setup

이 문서는 Catch-CBNU 프로젝트의 PostgreSQL 데이터베이스 생성 방법을 설명한다.

## 1. PostgreSQL 설치

PostgreSQL을 설치한다.

설치 시 다음 구성 요소를 포함한다.

```txt
PostgreSQL Server
pgAdmin 4
Command Line Tools
```

설치 중 설정한 비밀번호는 나중에 DB 접속 시 필요하므로 반드시 기억한다.

## 2. pgAdmin 4 실행

PostgreSQL 설치 후 `pgAdmin 4`를 실행한다.

왼쪽 메뉴에서 서버를 펼친다.

```txt
Servers
  PostgreSQL
```

비밀번호를 입력하라는 창이 나오면 PostgreSQL 설치 시 설정한 비밀번호를 입력한다.

## 3. 데이터베이스 생성

왼쪽 메뉴에서 `Databases`를 우클릭한 뒤 아래 메뉴를 선택한다.

```txt
Create > Database...
```

데이터베이스 이름은 다음과 같이 입력한다.

```txt
catch_cbnu
```

입력 후 `Save`를 클릭한다.

## 4. schema.sql 실행

왼쪽 메뉴에서 방금 생성한 `catch_cbnu` 데이터베이스를 선택한다.

이후 상단 메뉴에서 다음을 선택한다.

```txt
Tools > Query Tool
```

Query Tool이 열리면 상단의 폴더 아이콘을 클릭한다.

다음 파일을 선택한다.

```txt
docs/database/schema.sql
```

파일이 열리면 실행 버튼을 클릭한다.

```txt
Execute / ▶ 버튼
```

정상적으로 실행되면 테이블이 생성된다.

## 5. 생성 확인

왼쪽 메뉴에서 다음 경로를 펼친다.

```txt
catch_cbnu
  Schemas
    public
      Tables
```

`Tables`를 우클릭한 뒤 `Refresh`를 클릭한다.

다음 테이블들이 보이면 정상적으로 생성된 것이다.

```txt
activities
characters
sensors
submissions
user_characters
users
```

## 6. DB 접속 정보

서버 개발자는 다음 정보를 사용해 DB에 접속할 수 있다.

```txt
DBMS: PostgreSQL
Host: localhost
Port: 5432
Database: catch_cbnu
Username: postgres
Password: PostgreSQL 설치 시 설정한 비밀번호
```

## 7. 서버 연동 방식

Android 앱은 DB에 직접 접속하지 않는다.

전체 구조는 다음과 같다.

```txt
Android 앱
-> 서버 API
-> PostgreSQL DB
```

서버는 `schema.sql`로 생성된 테이블을 사용해 사용자, 센서, 제출 기록, 캐릭터, 활동 기록 데이터를 저장하고 조회한다.
