A simple app which loads three json files from assets (`batters.json`, `colors.json`, `widget.json`) into  a Realm database.
Before it is ready progress is shown to indicate that something is loading for the user. Once ready it is shown in main activity UI.

UI is nothing more fancy than a `Spinner` for selecting what file to view and a `TextView` for displaying its content.

For code architecture I've tried to keep logic out of the view (`MainActivity` in this case) and keep it in `ContentViewModel`. Thus the view is only responsible for observing data changes and forward input.