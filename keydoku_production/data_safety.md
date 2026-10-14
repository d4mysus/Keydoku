# Data handling

This reference describes the app's current behavior. It is not a completed
Google Play Data safety submission.

| Category | Behavior |
| --- | --- |
| Personal data collected or shared | None by the app |
| Accounts, ads, and analytics | None |
| App permissions | None declared |
| Saved data | Puzzle, notes, timer, settings, and statistics stored locally |
| Cloud backup and device transfer | Disabled |
| QR generation | Local ZXing core library; no data transmission |
| Donations | Optional address display or external browser link |

Opening Buy Me a Coffee transfers control to the user's browser.
That service handles information supplied on its website under its own policy.
Bitcoin donation information is displayed locally; Keydoku does not initiate
a payment or transmit wallet data.

Users can delete local game data by clearing app storage or uninstalling.
The app does not synchronize or restore data after deletion.

See the [privacy policy](privacy_policy.md).
