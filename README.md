![Kotlin](https://img.shields.io/badge/Kotlin-1.4-blue?style=flat&logo=kotlin)
![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/Vonvikken/Vonvikken-Notification-Bot-Kotlin)
![GitHub last commit](https://img.shields.io/github/last-commit/Vonvikken/Vonvikken-Notification-Bot-Kotlin)
![GitHub](https://img.shields.io/github/license/Vonvikken/Vonvikken-Notification-Bot-Kotlin)
[![pre-commit](https://img.shields.io/badge/pre--commit-enabled-brightgreen?logo=pre-commit&logoColor=white)](https://github.com/pre-commit/pre-commit)
[![ktlint](https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg)](https://ktlint.github.io/)

# Notification Bot (Kotlin)

Telegram Bot for receiving notifications (_Kotlin version_).

## Description

I create this simple Telegram bot for two reasons: first, make practice with the Kotlin programming language, that I'm
starting to learn; second, find a way to receive system notifications from the small server I installed on a Raspberry
Pi at home (backups, package updates...). It's very simple, but I plan to add more features if needed.

This program listens for messages on a UNIX-domain socket, parses them and sends them to a given Telegram group.

## Prerequisites

First you need to create a bot with BotFather
(see [here](https://core.telegram.org/bots#3-how-do-i-create-a-bot)) and get the authorization token, then add the bot
to a group and retrieve its chat ID (you can see how to do
it [here](https://stackoverflow.com/questions/32423837/telegram-bot-how-to-get-a-group-chat-id)).

To build the bot, you need [Apache Maven](https://maven.apache.org/) correctly installed and configured.

The bot runs on Linux on JRE 1.8 or later, so make sure it is installed in your system.

## Installation

After cloning the repository, go to the project directory and run:

```bash
mvn package
```

When the build process is completed, copy the file `target/notification-bot-<version>-jar-with-dependencies.jar` to a
directory of your choice and create a [configuration](#configuration) file.

## Configuration

The program needs a configuration file in JSON format to run. The default location is `config.json` in the program
directory, but it can be changed via the `--config`/`-c` option.

The file format is the following:

```json
{
  "socket_path": "/path/to/socket",
  "token": "AUTHORIZATION_TOKEN",
  "chat_id": -123456789
}
```

* `socket_path` is optional, and contains the path to the UNIX-domain socket used for receiving messages. The default
  value is `/var/tmp/notificationbot.sock`.
* `token` is the bot authorization token received by BotFather.
* `chat_id` is the numeric chat ID of the group the bot sends notifications to.

Make sure to change the config file permissions in order to make it visible only to its user (e.g. `r--------`), since
it contains your authorization token in plain text.

## Usage

To start the program, run the following command:

```bash
java -jar notification-bot-<version>-jar-with-dependencies.jar
```

You might also want to run it as a service and start it at system boot.

Upon start, the program creates the socket at the specified location; you can send a string to it, for example using the
Netcat utility:

```bash
echo -n "My notification message" | nc -U /path/to/socket
```

The socket will be deleted when terminating the program.

### Message format

You can send both formatted text, using the [HTML markup](https://core.telegram.org/bots/api#html-style), and emojis,
either using their HTML code or their alias (see [here](https://github.com/vdurmont/emoji-java/blob/master/EMOJIS.md)
for the complete list, they are likely the same used in the GitHub Markdown). For example, the following string:

```html
&#9888; <i>Warning!</i> I found a :bug: in the code!
```

is rendered in Telegram as follows:

> &#9888; _Warning!_ I found a :bug: in the code!

### Commands
You can issue the following commands to the bot:
* `/serviceStop` stop the service listening on the notification socket.
* `/serviceStart` start the service listening on the notification socket.
* `/serviceInfo` print the status of the service that listens on the notification socket.
* `/help` print this command list.

## Contributing

Bug reports and pull requests are welcome on GitHub at https://github.com/vonvikken/Vonvikken-Notification-Bot-Kotlin.
This project is intended to be a safe, welcoming space for collaboration, and contributors are expected to adhere to the
[code of conduct](https://github.com/vonvikken/Vonvikken-Notification-Bot-Kotlin/blob/master/CODE_OF_CONDUCT.md).

## Code of Conduct

Everyone interacting in the Notification Bot project's codebases, issue trackers, chat rooms and mailing lists is
expected to follow
the [code of conduct](https://github.com/vonvikken/Vonvikken-Notification-Bot-Kotlin/blob/master/CODE_OF_CONDUCT.md).

## License

This project is distributed under _MIT_ license.
