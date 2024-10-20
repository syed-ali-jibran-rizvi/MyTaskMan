# Smart Task Management App

## Overview

The **Smart Task Management Android App** is designed to revolutionize how users manage their daily tasks through the use of **AI-powered task prioritization** and **context-aware scheduling**. The app dynamically adapts to real-time contextual data, such as user location, activity, and time, to intelligently rank and schedule tasks. This project leverages advanced machine learning models and provides a highly personalized experience for users, all while maintaining strong privacy standards.



---

## Features

- **AI-Powered Task Prioritization**: Uses **Google Gemini's LLM API** to analyze and prioritize tasks based on urgency, importance, and contextual relevance.
- **Context-Aware Scheduling**: Dynamically schedules tasks using real-time context data such as the user’s current location, activity, and time of day.
- **Firebase Integration**: Real-time data synchronization with Firebase for event creation and task updates.
- **Privacy-Focused**: Processes sensitive user data locally to ensure privacy while maintaining personalized task management.

---

## How It Works

1. **Context Collection**: The app gathers contextual data (location, time, and user activity) in real time.
2. **Context Analysis**: Using the **Hugging Face LLM API**, the system evaluates the relevance and urgency of tasks based on the user's current context.
3. **Task Prioritization**: Tasks are ranked using a custom prioritization logic that factors in urgency, importance, and real-time context.
4. **Dynamic Scheduling**: High-priority tasks are scheduled for completion based on user availability, with tasks and priorities updated dynamically.

![Use Case](./images/Picture3.png) <!-- Replace with actual image path -->
![DFD Level 0](./images/Picture1.png) <!-- Replace with actual image path -->
![DFD Level 1](./images/Picture2.png) <!-- Replace with actual image path -->
