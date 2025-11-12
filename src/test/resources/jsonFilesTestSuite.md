

## **JulesAPI v1alpha: Corrected Mock JSON Test Suite**

This compendium provides a comprehensive set of mock JSON files for all v1alpha API methods, regenerated to fix previous formatting errors. The mocks are based on the official REST API reference.1

---

## **Part 1: v1alpha.sources**

Methods for the v1alpha.sources resource.2

### **Method: sources.get**

* **Request Body:** (Empty)
* **Response Body:** (Maximal representation of a Source object 2)

// FILENAME: getSource\_response.json

JSON

{  
"name": "sources/github/google/jules-demo-repo",  
"id": "github/google/jules-demo-repo",  
"githubRepo": {  
"owner": "google",  
"repo": "jules-demo-repo",  
"isPrivate": false,  
"defaultBranch": {  
"displayName": "main"  
},  
"branches": \[  
{  
"displayName": "main"  
},  
{  
"displayName": "develop"  
},  
{  
"displayName": "feature/new-login"  
}  
\]  
}  
}

### **Method: sources.list**

* **Request Body:** (Empty)
* **Response Bodies:** (Based on listSources response schema 4)

// FILENAME: listSources\_response\_empty.json

JSON

{  
"sources":  
}

// FILENAME: listSources\_response\_populated.json

JSON

{  
"sources":  
}  
},  
{  
"name": "sources/github/my-org/internal-tools",  
"id": "github/my-org/internal-tools",  
"githubRepo": {  
"owner": "my-org",  
"repo": "internal-tools",  
"isPrivate": true,  
"defaultBranch": {  
"displayName": "production"  
},  
"branches": \[  
{  
"displayName": "production"  
},  
{  
"displayName": "staging"  
}  
\]  
}  
}  
\]  
}

// FILENAME: listSources\_response\_paginated.json

JSON

{  
"sources":  
}  
}  
\],  
"nextPageToken": "abc-123-xyz-789-token-for-next-page"  
}

---

## **Part 2: v1alpha.sessions**

Methods for the v1alpha.sessions resource.3

### **Method: sessions.create**

* **Request Bodies:** (Based on Session resource "Input" fields 3)

// FILENAME: createSession\_request\_minimal.json

JSON

{  
"prompt": "Please add a new /health endpoint to the main server.",  
"sourceContext": {  
"source": "sources/github/google/jules-demo-repo",  
"githubRepoContext": {  
"startingBranch": "main"  
}  
}  
}

// FILENAME: createSession\_request\_maximal.json

JSON

{  
"prompt": "Refactor the entire authentication module to use OIDC, and add unit tests for all new code.",  
"sourceContext": {  
"source": "sources/github/my-org/internal-tools",  
"githubRepoContext": {  
"startingBranch": "staging"  
}  
},  
"title": "OIDC Authentication Refactor",  
"requirePlanApproval": true,  
"automationMode": "AUTO\_CREATE\_PR"  
}

// FILENAME: createSession\_request\_no\_approval.json

JSON

{  
"prompt": "Fix the typo on the landing page.",  
"sourceContext": {  
"source": "sources/github/google/jules-demo-repo",  
"githubRepoContext": {  
"startingBranch": "main"  
}  
},  
"title": "Fix landing page typo",  
"requirePlanApproval": false,  
"automationMode": "AUTO\_CREATE\_PR"  
}

// FILENAME: createSession\_request\_default\_automation.json

JSON

{  
"prompt": "Fix the typo on the landing page.",  
"sourceContext": {  
"source": "sources/github/google/jules-demo-repo",  
"githubRepoContext": {  
"startingBranch": "main"  
}  
},  
"title": "Fix landing page typo",  
"requirePlanApproval": false,  
"automationMode": "AUTOMATION\_MODE\_UNSPECIFIED"  
}

* **Response Body:** (Based on Session resource "Output" fields.3 This is the newly created instance.5)

// FILENAME: createSession\_response.json

JSON

{  
"name": "sessions/abc-123",  
"id": "abc-123",  
"prompt": "Please add a new /health endpoint to the main server.",  
"sourceContext": {  
"source": "sources/github/google/jules-demo-repo",  
"githubRepoContext": {  
"startingBranch": "main"  
}  
},  
"title": "Add /health endpoint",  
"createTime": "2025-10-07T10:00:00.000000Z",  
"updateTime": "2025-10-07T10:00:01.000000Z",  
"state": "QUEUED",  
"url": "https://jules.google.com/app/session/abc-123",  
"outputs":  
}

### **Method: sessions.get**

* **Request Body:** (Empty)
* **Response Bodies:** (Permutations of a Session object based on state 3 and outputs 3)

// FILENAME: getSession\_response\_queued.json

JSON

{  
"name": "sessions/abc-123",  
"id": "abc-123",  
"prompt": "Please add a new /health endpoint to the main server.",  
"sourceContext": {  
"source": "sources/github/google/jules-demo-repo",  
"githubRepoContext": {  
"startingBranch": "main"  
}  
},  
"title": "Add /health endpoint",  
"createTime": "2025-10-07T10:00:00.000000Z",  
"updateTime": "2025-10-07T10:00:01.000000Z",  
"state": "QUEUED",  
"url": "https://jules.google.com/app/session/abc-123",  
"outputs":  
}

// FILENAME: getSession\_response\_awaiting\_approval.json

JSON

{  
"name": "sessions/def-456",  
"id": "def-456",  
"prompt": "Refactor the entire authentication module...",  
"sourceContext": {  
"source": "sources/github/my-org/internal-tools",  
"githubRepoContext": {  
"startingBranch": "staging"  
}  
},  
"title": "OIDC Authentication Refactor",  
"createTime": "2025-10-07T11:00:00.000000Z",  
"updateTime": "2025-10-07T11:05:00.000000Z",  
"state": "AWAITING\_PLAN\_APPROVAL",  
"url": "https://jules.google.com/app/session/def-456",  
"outputs":  
}

// FILENAME: getSession\_response\_completed\_with\_output.json

JSON

{  
"name": "sessions/xyz-987",  
"id": "xyz-987",  
"prompt": "Fix the typo on the landing page.",  
"sourceContext": {  
"source": "sources/github/google/jules-demo-repo",  
"githubRepoContext": {  
"startingBranch": "main"  
}  
},  
"title": "Fix landing page typo",  
"createTime": "2025-10-09T09:00:00.000000Z",  
"updateTime": "2025-10-09T09:15:00.000000Z",  
"state": "COMPLETED",  
"url": "https://jules.google.com/app/session/xyz-987",  
"outputs":  
}

// FILENAME: getSession\_response\_failed.json

JSON

{  
"name": "sessions/err-321",  
"id": "err-321",  
"prompt": "Migrate database from v1 to v2.",  
"sourceContext": {  
"source": "sources/github/my-org/internal-tools",  
"githubRepoContext": {  
"startingBranch": "main"  
}  
},  
"title": "Database Migration v1-v2",  
"createTime": "2025-10-06T12:00:00.000000Z",  
"updateTime": "2025-10-06T18:00:00.000000Z",  
"state": "FAILED",  
"url": "https://jules.google.com/app/session/err-321",  
"outputs":  
}

### **Method: sessions.list**

* **Request Body:** (Empty)
* **Response Bodies:** (Based on listSessions response schema 6)

// FILENAME: listSessions\_response\_empty.json

JSON

{  
"sessions":  
}

// FILENAME: listSessions\_response\_populated.json

JSON

{  
"sessions":  
},  
{  
"name": "sessions/def-456",  
"id": "def-456",  
"prompt": "Refactor the entire authentication module...",  
"sourceContext": {  
"source": "sources/github/my-org/internal-tools",  
"githubRepoContext": {  
"startingBranch": "staging"  
}  
},  
"title": "OIDC Authentication Refactor",  
"createTime": "2025-10-07T11:00:00.000000Z",  
"updateTime": "2025-10-07T11:05:00.000000Z",  
"state": "AWAITING\_PLAN\_APPROVAL",  
"url": "https://jules.google.com/app/session/def-456",  
"outputs":  
}  
\]  
}

// FILENAME: listSessions\_response\_paginated.json

JSON

{  
"sessions":  
}  
\],  
"nextPageToken": "pager-token-456"  
}

### **Method: sessions.approvePlan**

* **Request Body:** (The request body *must* be empty 7)

// FILENAME: approvePlan\_request.json

JSON

{}

* **Response Body:** (The response body is empty.7 An empty JSON is provided for test harness compatibility.)

// FILENAME: approvePlan\_response.json

JSON

{}

### **Method: sessions.sendMessage**

* **Request Body:** (Contains a prompt string 8)

// FILENAME: sendMessage\_request.json

JSON

{  
"prompt": "That plan looks good, but please add unit tests for the new module as well."  
}

* **Response Body:** (The response body is empty.8 An empty JSON is provided for test harness compatibility.)

// FILENAME: sendMessage\_response.json

JSON

{}

---

## **Part 3: v1alpha.sessions.activities**

This resource represents individual events within a session.9

**Note on Activity Schema:** The API reference documents the get 11 and list 12 methods for sessions.activities and states they return Activity objects. However, the documentation *does not provide the JSON schema for the Activity object itself*.

Therefore, the listActivities responses are mocked with an empty activities array, as the content of those objects is undefined. The getActivity response cannot be mocked for the same reason.

### **Method: sessions.activities.get**

* **Request Body:** (The request body must be empty 11)

// FILENAME: getActivity\_request.json

JSON

{}

* **Response Body:**
    * // FILENAME: getActivity\_response.json
    * (Cannot be mocked. The documentation 11 states this "contains an instance of Activity," but the schema for Activity is not defined.)

### **Method: sessions.activities.list**

* **Request Body:** (Empty)
* **Response Bodies:** (Based on listActivities response schema.12 The activities array is empty as the object schema is undefined.)

// FILENAME: listActivities\_response\_empty.json

JSON

{  
"activities":  
}

// FILENAME: listActivities\_response\_paginated.json

JSON

{  
"activities":,  
"nextPageToken": "act-pager-789-next"  
}

#### **Works cited**

1. Jules API \- Google for Developers, accessed November 12, 2025, [https://developers.google.com/jules/api/reference/rest](https://developers.google.com/jules/api/reference/rest)
2. REST Resource: sources | Jules API | Google for Developers, accessed November 12, 2025, [https://developers.google.com/jules/api/reference/rest/v1alpha/sources](https://developers.google.com/jules/api/reference/rest/v1alpha/sources)
3. REST Resource: sessions | Jules API \- Google for Developers, accessed November 12, 2025, [https://developers.google.com/jules/api/reference/rest/v1alpha/sessions](https://developers.google.com/jules/api/reference/rest/v1alpha/sessions)
4. Method: sources.list | Jules API | Google for Developers, accessed November 12, 2025, [https://developers.google.com/jules/api/reference/rest/v1alpha/sources/list](https://developers.google.com/jules/api/reference/rest/v1alpha/sources/list)
5. Method: sessions.create | Jules API \- Google for Developers, accessed November 12, 2025, [https://developers.google.com/jules/api/reference/rest/v1alpha/sessions/create](https://developers.google.com/jules/api/reference/rest/v1alpha/sessions/create)
6. Method: sessions.list | Jules API \- Google for Developers, accessed November 12, 2025, [https://developers.google.com/jules/api/reference/rest/v1alpha/sessions/list](https://developers.google.com/jules/api/reference/rest/v1alpha/sessions/list)
7. Method: sessions.approvePlan | Jules API \- Google for Developers, accessed November 12, 2025, [https://developers.google.com/jules/api/reference/rest/v1alpha/sessions/approvePlan](https://developers.google.com/jules/api/reference/rest/v1alpha/sessions/approvePlan)
8. Method: sessions.sendMessage | Jules API | Google for Developers, accessed November 12, 2025, [https://developers.google.com/jules/api/reference/rest/v1alpha/sessions/sendMessage](https://developers.google.com/jules/api/reference/rest/v1alpha/sessions/sendMessage)
9. Jules API | Google for Developers, accessed November 12, 2025, [https://developers.google.com/jules/api](https://developers.google.com/jules/api)
10. Level Up Your Dev Game: The Jules API is Here\! \- Google Developers Blog, accessed November 12, 2025, [https://developers.googleblog.com/en/level-up-your-dev-game-the-jules-api-is-here/](https://developers.googleblog.com/en/level-up-your-dev-game-the-jules-api-is-here/)
11. Method: sessions.activities.get | Jules API | Google for Developers, accessed November 12, 2025, [https://developers.google.com/jules/api/reference/rest/v1alpha/sessions.activities/get](https://developers.google.com/jules/api/reference/rest/v1alpha/sessions.activities/get)
12. Method: sessions.activities.list | Jules API \- Google for Developers, accessed November 12, 2025, [https://developers.google.com/jules/api/reference/rest/v1alpha/sessions.activities/list](https://developers.google.com/jules/api/reference/rest/v1alpha/sessions.activities/list)