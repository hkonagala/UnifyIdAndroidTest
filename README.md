# UnifyIdAndroidTest

## Technique
1. Created an activity with a single button and upon clicking created a repeating task which will open camera and take a picture.
2. Upon clicking the picture it will be saved in the SQLite database with the timestamp and the task will place itself in the queue after 500ms to take another picture.
3. After 10 images, the repeating task will be stopped.
4. Once the pictures are collected, the byte array is converted into Base64 string and stored in the SQLite database.
5. Tried to not show the user that the picture is being taken.


## Errors
1. The takePicture is not functioning properly, probably an error with the phone that I use. I will check with other devices soon.
2. The Camera API that I once worked on earlier is deprecated now and the latest code in the documentation seems to be incomplete. This might work with a simple change in the API call sequence.

## Future Work
1. The base64 string can be further encrypted to make it not readable by the ones that have access to database.
2. Taking a video and processing the 0.5s delayed images might give exact time separated images and could be faster.
