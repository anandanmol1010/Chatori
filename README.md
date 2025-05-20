# Chatori - Street Food Discovery App

Chatori is an Android application designed to help users discover the best street food stalls around them. The app provides a platform for users to explore, review, and share their favorite street food experiences.

## Features

### User Authentication
- Email/Password login and registration
- Google Sign-In integration
- User profile management

### Stall Discovery
- Home screen with recommended, nearby, and top-rated stalls
- Advanced search with filters for dish type, area, and rating
- Map view to find stalls based on location

### Stall Management
- Add new stalls with images, location, and details
- View detailed information about stalls
- Get directions to stalls

### Reviews and Ratings
- Write reviews for stalls
- View all reviews for a stall
- Rate stalls on a 5-star scale

### User Profile
- View and edit profile information
- Track favorite stalls
- View stalls added by the user
- View reviews written by the user

## Architecture

The app follows a clean architecture approach with the following components:

### Model Layer
- Data models: Stall, User, Review, Dish
- Repository classes for data access

### UI Layer
- Activities and Fragments for UI presentation
- Adapters for RecyclerViews
- ViewModels for UI state management

### Data Layer
- Firebase Firestore for database
- Firebase Authentication for user management
- Firebase Storage for image storage
- Google Maps for location services

## Setup Instructions

1. Clone the repository
2. Open the project in Android Studio
3. Replace the `google-services.json` file with your own Firebase project configuration
4. Replace the Google Maps API key in `strings.xml` with your own key
5. Build and run the application

## Dependencies

- Firebase (Auth, Firestore, Storage)
- Google Maps and Location Services
- Glide for image loading
- Material Design components
- CircleImageView for profile pictures

## Screenshots

(Screenshots will be added here)

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Material Design for UI components
- Firebase for backend services
- Google Maps for location services
