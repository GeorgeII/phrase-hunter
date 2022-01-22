# phrase-hunter

[![Build Status](https://travis-ci.com/GeorgeII/phrase-hunter.svg?branch=master)](https://travis-ci.com/GeorgeII/phrase-hunter)


The application searches for exact occurrences of a given phrase in provided movies and videos with subtitles. It sends all 
the info of such occurrences and plays movies/videos starting from the phrase timestamp.

<br />

Launching:
```bash
sbt docker:publishLocal
```

```bash
docker-compose up -d
```

There is a pgAdmin service. You can go to [localhost:5050](http://localhost:5050) for it. The default pgAdmin password is _admin_.
Servers -> create -> Server. Enter name: _phrase-hunter-server_, connection hostname: _postgres_container_, password: _password_.