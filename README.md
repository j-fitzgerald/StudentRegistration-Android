# StudentRegistration-Android
<Server Offline>Student register, waitlist, and drop courses

The backend server to this project is now offline, and will not work.

Overview:
Android application to allow a student to:
- register with the backend server
- search for available courses
- register/waitlist for courses
- view their current courses
- unregister/waitlist selected courses

Using the Application:
Setup a new student and register with the server if one does not alreadhy exist
- Student data is stored on the phone, and loaded on start
View my courses:
- Returns the current courses, split by Currently Registered and Waitlisted
- Select any course to remove it from Currently Registered or Waitlist
- Option to delete the local student file for testing <Only 1 student per device is based on assignment restriction>
Search for courses:
- Must select a Subject, and can further filter by times and course levels
- If search results are excessive (over 200 results) requests further filtration
Search Results:
- Display the search results, displaying important course data
- select any course to display the full course details
- allow student to register/waitlist for the course
