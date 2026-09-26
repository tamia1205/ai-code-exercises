 # User List Debugging Example

This example demonstrates common debugging scenarios in frontend JavaScript, specifically focusing on user list rendering with intentional bugs and their solutions.

1. `userList.js` - demonstrate an error caused by an index out of bounds issue — run the `userList.test.js` unittest to see
2. `taskManager.js` - demonstrate error due to a global variable being overwritten - run `taskManager.test.js` unittest to see

## Prerequisites
- Node.js 14.x or higher
- A modern web browser with developer tools
- Basic understanding of DOM manipulation

## Setup and Running

1. Install dependencies:
   ```
   npm install
   ```

2. Run the application:
   ```
   npm start
   ```

## Testing

To run all tests:
```
npm test
```

To run individual tests:
```
npx jest tests/userList.test.js
npx jest tests/taskManager.test.js
```

You can also run tests with a specific pattern:
```
npx jest --testNamePattern="loadDashboard should render user list correctly"
```

