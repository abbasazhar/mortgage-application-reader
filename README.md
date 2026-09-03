# Mortgage Application Reader

A basic Spring Boot backend that reads mortgage-case applications.

This was built as an interview task and is deliberately modeled on
[Mortartec](https://mortartec.com/)'s own domain: Mortartec is a UK
mortgage/protection brokerage back-office platform that ingests a case
(applicant + property details), enriches it with external data, scores it,
and tracks it through to placement with a lender. This service implements a
small slice of that same pipeline:

```
POST /api/applications
      │
      ▼
 validate the submitted case
      │
      ▼
 enrich it with PUBLICLY AVAILABLE data
 (postcodes.io — free UK postcode/region lookup, no API key)
      │
      ▼
 save the enriched case to LOCAL STORAGE
 (a JSON file on disk — no external database)
      │
      ▼
GET /api/applications
GET /api/applications/{id}     → read the saved case back
```

## Why these choices

| Requirement (from the task)              | How it's implemented |
|-------------------------------------------|-----------------------|
| Spring Boot                                | Java 17+, Spring Boot 3.4.1, Maven |
| POST endpoint                              | `POST /api/applications` |
| Use publicly available details             | Calls the free, public [postcodes.io](https://postcodes.io) API to resolve the submitted UK postcode into region / local authority / country / coordinates — the kind of lookup a mortgage brokerage does when placing a case with a lender |
| Local storage                              | `FileBackedApplicationRepository` persists to `./data/applications.json` on the local filesystem (with an in-memory cache), so no database setup is required and data survives a restart |
| GET endpoint to bring it back               | `GET /api/applications` (list) and `GET /api/applications/{id}` (single record) |
| Testable from a terminal                    | Everything below is plain `curl` — no UI required |

## Project layout

```
src/main/java/com/mortartec/appreader/
├── MortgageApplicationReaderApplication.java   # main class + RestTemplate bean
├── controller/
│   ├── ApplicationController.java              # POST / GET endpoints
│   └── GlobalExceptionHandler.java             # clean 400s on invalid input
├── dto/
│   └── ApplicationRequest.java                 # validated POST request body
├── model/
│   └── MortgageApplication.java                # the stored/returned case
├── client/
│   ├── PublicPostcodeClient.java                # calls the public postcodes.io API
│   ├── PostcodeLookupResponse.java
│   └── PostcodeLookupResult.java
├── repository/
│   ├── LocalStorageApplicationRepository.java   # storage interface
│   └── FileBackedApplicationRepository.java     # JSON-file local storage impl
└── service/
    └── ApplicationService.java                  # orchestrates enrich + save + read
```

## Requirements

- Java 17+
- Maven 3.6+ (or use the included `mvnw` wrapper if you add one)
- Internet access only for the enrichment call to postcodes.io at runtime (the
  build itself needs internet the first time, to download dependencies from
  Maven Central)

## Build & run

```bash
# from the project root
mvn clean package
java -jar target/mortgage-application-reader-1.0.0.jar

# or, for local development
mvn spring-boot:run
```

The service starts on **http://localhost:8080**. A `data/` folder is created
next to wherever you run it from — that's the local storage file.

## Testing it from a terminal (what the interviewer will do)

**1. Submit a new mortgage application (POST):**

```bash
curl -s -X POST http://localhost:8080/api/applications \
  -H "Content-Type: application/json" \
  -d '{
        "applicantName": "Jordan Smith",
        "email": "jordan.smith@example.com",
        "propertyPostcode": "SW1A 1AA",
        "loanAmount": 240000,
        "propertyValue": 300000
      }' | python3 -m json.tool
```

Expected response (`201 Created`) — note the enriched fields (`region`,
`adminDistrict`, `country`, `latitude`, `longitude`) that came from the
public postcodes.io lookup, and the derived `loanToValuePercent`:

```json
{
    "id": "b6f2b7b1-....",
    "applicantName": "Jordan Smith",
    "email": "jordan.smith@example.com",
    "propertyPostcode": "SW1A 1AA",
    "loanAmount": 240000,
    "propertyValue": 300000,
    "region": "London",
    "adminDistrict": "Westminster",
    "country": "England",
    "latitude": 51.501,
    "longitude": -0.141,
    "loanToValuePercent": 80.00,
    "caseStatus": "ENRICHED",
    "submittedAt": "2026-09-03T12:00:00Z"
}
```

**2. List every stored application (GET):**

```bash
curl -s http://localhost:8080/api/applications | python3 -m json.tool
```

**3. Read back a single application by id (GET):**

```bash
# swap in the "id" value returned from step 1
curl -s http://localhost:8080/api/applications/<id> | python3 -m json.tool
```

**4. Validation (bad input returns 400, not a stack trace):**

```bash
curl -s -X POST http://localhost:8080/api/applications \
  -H "Content-Type: application/json" \
  -d '{
        "applicantName": "Jordan Smith",
        "email": "not-an-email",
        "propertyPostcode": "NOT-A-POSTCODE",
        "loanAmount": -5,
        "propertyValue": 300000
      }' | python3 -m json.tool
```

## Notes / design decisions

- **Local storage, not a database.** The task asked for local storage, so
  applications are kept in a JSON file (`./data/applications.json`) rather
  than standing up Postgres/MySQL/H2. This keeps the project runnable with
  zero setup while still surviving a restart (a plain in-memory list would
  not).
- **Enrichment is best-effort.** If postcodes.io is unreachable or the
  postcode isn't recognised, the case is still saved (`caseStatus` becomes
  `RECEIVED_NO_ENRICHMENT`) rather than failing the whole request — a public
  third-party API should never be a single point of failure for saving a
  case.
- **Loan-to-value** is calculated on submission (`loanAmount / propertyValue
  * 100`), a real figure a mortgage brokerage checks against every case.

## Running the tests

```bash
mvn test
```

`ApplicationControllerTest` exercises the full POST → GET flow through
`MockMvc` (with the public postcode client mocked, so tests don't depend on
network access) and confirms invalid input is rejected with a 400.
