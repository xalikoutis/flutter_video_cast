import 'package:flutter/material.dart';
import 'package:flutter_video_cast/flutter_video_cast.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
        home: CastSample()
    );
  }
}

class CastSample extends StatefulWidget {
  static const _iconSize = 50.0;

  @override
  _CastSampleState createState() => _CastSampleState();
}

class _CastSampleState extends State<CastSample> {
  ChromeCastController? _controller;
  AppState _state = AppState.idle;
  bool _playing = false;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Plugin example app'),
        actions: <Widget>[
          AirPlayButton(
            size: CastSample._iconSize,
            color: Colors.white,
            activeColor: Colors.amber,
            onRoutesOpening: () => print('opening'),
            onRoutesClosed: () => print('closed'),
          ),
          ChromeCastButton(
            size: CastSample._iconSize,
            color: Colors.black,
            onButtonCreated: _onButtonCreated,
            onSessionStarted: _onSessionStarted,
            onSessionEnded: () => setState(() => _state = AppState.idle),
            onRequestCompleted: _onRequestCompleted,
            onRequestFailed: _onRequestFailed,
            onProgressChanged: _onProgressChanged,
          ),
        ],
      ),
      body: Center(child: _handleState()),
    );
  }

  _onProgressChanged(int progress) {
    print('progress: $progress');
  }

  Widget _handleState() {
    switch(_state) {
      case AppState.idle:
        return Text('ChromeCast not connected');
      case AppState.connected:
        return Text('No media loaded');
      case AppState.mediaLoaded:
        return _mediaControls();
      case AppState.error:
        return Text('An error has occurred');
      default:
        return Container();
    }
  }

  Widget _mediaControls() {
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: <Widget>[
        _RoundIconButton(
          icon: Icons.replay_10,
          onPressed: () => _controller?.seek(relative: true, interval: -10.0),
        ),
        _RoundIconButton(
            icon: _playing
                ? Icons.pause
                : Icons.play_arrow,
            onPressed: _playPause
        ),
        _RoundIconButton(
          icon: Icons.forward_10,
          onPressed: () => _controller?.seek(relative: true, interval: 10.0),
        )
      ],
    );
  }

  Future<void> _playPause() async {
    final playing = await _controller?.isPlaying();
    if(playing ==null) return;
    if(playing) {
      await _controller?.pause();
    } else {
      await _controller?.play();
    }
    setState(() => _playing = !playing);
  }

  Future<void> _onButtonCreated(ChromeCastController controller) async {
    _controller = controller;
    await _controller?.addSessionListener();
  }

  Future<void> _onSessionStarted() async {
    setState(() => _state = AppState.connected);
    await _controller?.loadMedia('https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4');
    //await _controller?.loadMedia('https://player.vimeo.com/progressive_redirect/download/586780228/container/af9732b5-bc39-47d4-b62b-01dd613050fd/5f618c0f/space_travel_short_film.mp4%20%28360p%29.mp4?expires=1664283745&loc=external&oauth2_token_id=317388570&signature=400c8143144aba4c1957f01d09497e3cea7bd9079376d632fd3607f1669b893c');
  }

  Future<void> _onRequestCompleted() async {
    final playing = await _controller?.isPlaying();
    if(playing ==null) return;
    setState(() {
      _state = AppState.mediaLoaded;
      _playing = playing;
    });
  }

  Future<void> _onRequestFailed(String error) async {
    setState(() => _state = AppState.error);
    print(error);
  }
}

class _RoundIconButton extends StatelessWidget {
  final IconData icon;
  final VoidCallback onPressed;

  _RoundIconButton({
    required this.icon,
    required this.onPressed
  });

  @override
  Widget build(BuildContext context) {
    return ElevatedButton(
        child: Icon(
            icon,
            color: Colors.white
        ),
        style: ElevatedButton.styleFrom(
            padding: EdgeInsets.all(16.0),
          foregroundColor: Colors.blue,
          shape: CircleBorder(),
        ),
        onPressed: onPressed
    );
  }
}

enum AppState {
  idle,
  connected,
  mediaLoaded,
  error
}
