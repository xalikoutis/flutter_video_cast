part of flutter_video_cast;

/// Callback method for when the button is ready to be used.
///
/// Pass to [ChromeCastButton.onButtonCreated] to receive a [ChromeCastController]
/// when the button is created.
typedef void OnButtonCreated(ChromeCastController controller);

/// Callback method for when a request has failed.
typedef void OnRequestFailed(String error);

typedef void OnProgressChanged(int progress);

/// Widget that displays the ChromeCast button.
class ChromeCastButton extends StatefulWidget {
  /// Creates a widget displaying a ChromeCast button.
  ChromeCastButton(
      {Key? key,
      this.size = 30.0,
      this.color = Colors.black,
      this.onButtonCreated,
      this.onSessionStarted,
      this.onSessionEnded,
      this.onRequestCompleted,
        this.onProgressChanged,
      this.onRequestFailed})
      : assert(
            defaultTargetPlatform == TargetPlatform.iOS ||
                defaultTargetPlatform == TargetPlatform.android,
            '$defaultTargetPlatform is not supported by this plugin'),
        super(key: key);

  /// The size of the button.
  final double size;

  /// The color of the button.
  /// This is only supported on iOS at the moment.
  final Color color;

  /// Callback method for when the button is ready to be used.
  ///
  /// Used to receive a [ChromeCastController] for this [ChromeCastButton].
  final OnButtonCreated? onButtonCreated;

  /// Called when a cast session has started.
  final VoidCallback? onSessionStarted;

  /// Called when a cast session has ended.
  final VoidCallback? onSessionEnded;

  /// Called when a cast request has successfully completed.
  final VoidCallback? onRequestCompleted;

  /// Called when a cast request has failed.
  final OnRequestFailed? onRequestFailed;

  final OnProgressChanged? onProgressChanged;

  late final ChromeCastController controller;

  @override
  State<ChromeCastButton> createState() => _ChromeCastButtonState();
}

class _ChromeCastButtonState extends State<ChromeCastButton> {

  @override
  void dispose() {
    super.dispose();
    widget.controller.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final Map<String, dynamic> args = {
      'red': widget.color.red,
      'green': widget.color.green,
      'blue': widget.color.blue,
      'alpha': widget.color.alpha
    };
    return SizedBox(
      width: widget.size,
      height: widget.size,
      child: _chromeCastPlatform.buildView(args, _onPlatformViewCreated),
    );
  }

  Future<void> _onPlatformViewCreated(int id) async {
    widget.controller = await ChromeCastController.init(id);
    if (widget.onButtonCreated != null) {
      widget.onButtonCreated!(widget.controller);
    }
    if (widget.onSessionStarted != null) {
      _chromeCastPlatform
          .onSessionStarted(id: id)
          ?.listen((_) => widget.onSessionStarted!());
    }
    if (widget.onSessionEnded != null) {
      _chromeCastPlatform
          .onSessionEnded(id: id)
          ?.listen((_) => widget.onSessionEnded!());
    }
    if (widget.onRequestCompleted != null) {
      _chromeCastPlatform
          .onRequestCompleted(id: id)
          ?.listen((_) => widget.onRequestCompleted!());
    }
    if (widget.onRequestFailed != null) {
      _chromeCastPlatform
          .onRequestFailed(id: id)
          ?.listen((event) => widget.onRequestFailed!(event.error));
    }

    if ( widget.onProgressChanged!= null) {
      _chromeCastPlatform
          .progressChanged(id: id)
          ?.listen((event) => widget.onProgressChanged!(event.progress));
    }
  }
}
