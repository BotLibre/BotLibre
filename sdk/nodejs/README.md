# BotLibre Nodejs SDK

The Bot Libre nodejs SDK allows you to easily integrate Bot Libre with a nodejs application. Through the SDK you can connect to your bots on Bot Libre, send messages, and train and configure your bots. You can also access Bot Libre analytics and AI services, and other content.

## Getting Started

### Prerequisites

- [Node.js](https://nodejs.org/)
- npm (Node Package Manager) 

### Installation
Clone the Bot Libre python SDK by clicking on this link:
https://github.com/BotLibre/BotLibre/tree/master/sdk/nodejs

### Navigate to the project directory:
```
cd nodejs
```
#### Install dependencies:
```
npm install
```
#### Note
Including separate scripts for development and production, with the development script utilizing nodemon for automatic server restarts.

### Scripts

#### Start application for development:
```
npm run dev
```
The "dev" script uses nodemon to watch for changes in your TypeScript files and automatically restarts the server when changes occur. It executes the TypeScript files directly using ts-node.
#### Start application for production:
```
npm start
```
The "start" script runs the compiled JavaScript files in the dist directory using Node.js.
