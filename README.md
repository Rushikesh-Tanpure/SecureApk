# Malicious Activity Detector

A powerful Android application designed to detect malicious activity and ensure secure browsing for users. The app offers a **7-day free trial** with an option to subscribe for continued access.

## Features

- Detect suspicious applications on the device.
- Verify the security status of URLs.
- 7-day free trial followed by a subscription requirement.
- Alert popups for subscription updates and trial status.
- Offline subscription management using `SharedPreferences`.

## Screenshots

### Home Screen
![Home Screen](images/home.png)

### Subscription Prompt
![Subscription Prompt](images/subscription.png)

### Payment Success Alert
![Payment Success Alert](images/payment.png)

## How It Works

1. **First Launch:**

   - The app automatically starts a **7-day free trial** when opened for the first time.
   - A confirmation alert notifies the user about the trial start.

2. **During Trial Period:**

   - Users are informed of the remaining trial days through an alert.

3. **After Trial Ends:**

   - The app restricts access until the user subscribes.
   - Clicking the "Buy" button directs the user to the `SubscriptionActivity` for payment.

4. **Payment Process:**

   - Upon successful payment, the subscription status and start date are stored locally in `SharedPreferences`.
   - Users are greeted with a "Subscription Activated" alert and redirected to the main app interface.

## Setup and Installation

1. Clone the repository:
   ```sh
   git clone https://github.com/Rushikesh-Tanpure/SecureApk.git
   ```
2. Open the project in **Android Studio**.
3. Sync the Gradle dependencies.
4. Build and run the project on an emulator or physical device.

## Technologies Used

- **Java** for core logic.
- **Android Studio** for development.
- **SharedPreferences** for offline subscription management.

## Contribution

Contributions are welcome! Feel free to submit issues or pull requests.

## License

This project is licensed under the **MIT License**.

