                                    A multipurpose Discord bot for my personal server.
                                              Not intended for distribution

                                                      Features (Out of date)


Stat Tracking:

Tracks all users' playtime per game and stores it in multiple MySQL databases. Data can be queried by users in the Discord server using the provided commands.



AI Integration:

The bot can send and receive messages through OpenAI's API. 
 Multiple commands use this, some can remember previous messages to form a conversation, something the API does not provide natively.
 Users can also query image generation through the API using DALLE-3
 Using voice recognition from porcupine, the bot can listen to users in voice chats and query an AI response from the API (Only listens when commanded to, data is not stored).
 The bot will detect if users are in a voice chat, and then detect if a common game is being played. 
         If so, users may use a command to receive a "tip" from the bot about that certain game. 
         There is a 50% chance it will pull this tip from a user-provided list of game tips stored in a MySQL database, otherwise, it will query the API for an AI-made tip. 
         The bot will display this tip in chat and say it in voice chat using OpenAI's text-to-speech model.
Uses OPENAI's Whisper model to convert speech to text, used in multiple commands. 
It can run commands and code based on what is said through speech-to-text and uses prompt engineering to convert text into runnable prompts in the code.


API Integration:

 The bot has multiple APIS implemented alongside OpenAI's. 
 The bot can provide info on road closures in Boca Chica, TX, as multiple people in my server are interested in SpaceX's Starship development program there.
The bot can display the next rocket launch and its info, with a filter for SpaceX only. It also sends out an alert if there is an imminent rocket launch from Vandenburg AFB.

 The bot can sometimes provide accurate information on the current Major Order in the game Helldivers 2. 
         The API has zero documentation and is not officially supported by Arrowhead Games, so it's hard to parse the info. 
         It can successfully parse the API when it is a certain type of major order, but the other types may not be possible.
 The bot can provide multiple-choice trivia questions pulled from an API and then read users' answers to determine if they are right or wrong.
 The bot can provide random memes from Reddit using a public API. It stores the info on each meme to make sure there are no repeats.
 The bot can tell a joke grabbed from a public API.



Audio Player: 

The bot can play audio from a provided YouTube link.
 Using LavaPlayer, a user can provide the bot a link to a YouTube video or live stream, and the bot will play the audio through the voice chat.
 Users can queue multiple videos to play and can skip videos, toggle repeat mode, and stop the audio.
                
