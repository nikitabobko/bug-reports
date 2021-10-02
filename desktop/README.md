# "Email as todo" approach on desktop

I don't know any analogical solution for desktop. On my Linux machine I use bunch of tiny
self written bash scripts (see [next section](#simple-bash-scripts-for-linux-but-it-should-be-possible-to-make-them-work-on-macos)).

I see two obvious workarounds:
1. Use web version of your email provider. I use Gmail and it's good. If you add your new
   [task-specific email](https://support.google.com/a/users/answer/9308648) to your
   [Google contacts](https://contacts.google.com/) then Gmail will add the email into completion
   for `To:` field.
2. Use desktop email client of your choice (though, I have never seen good client for Linux)

Anyway, I don't find it critical that such an app doesn't exist on desktop. Main reason for me
making such an app for Android was that it's not convenient to enter destination address every
time (+ integration with Android Tiles and Android sharing menu). On desktop, I have fully-fledged
keyboard and I can type fast so it's not a problem for me to fill in destination address every
time (thanks to `To:` field completion in Gmail).

Maybe, one day I will write fully-fledged `email-todo` solution for desktops (no, I won't).

# Simple bash scripts (for Linux but it should be possible to make them work on macOS)

Like I said, beside web version of Gmail, I also use bunch of self-written bash scripts which
you can observer in this directory. 4 Frontend commands: `todo`, `todo-work`, `todo-gui`,
`todo-work-gui` and 2 plumbing helper scripts: `_todo-base`, `_todo-gui-base`. It's simple
curl scripts which send email over SMTP.

`todo` and `todo-work` are intended for creating personal and work todos in cli respectively.
`*-gui` scripts open up a terminal with editor opened where X primary selection is prefilled.

## Installing those scripts on your Linux machine

0. Dependencies
   1. The scripts use [micro editor](https://micro-editor.github.io/) for interactive todo editing
      so you have to either install the editor or amend the scripts to use editor of your choice.
   2. `*-gui` scripts use [terminator terminal emulator](https://github.com/gnome-terminator/terminator)
      in order to show terminal window so you have to either install the emulator or amend the
      scripts to use terminal emulator of your choice.
   3. The scripts use [xclip](https://linux.die.net/man/1/xclip) to capture your X primary
      clipboard. You should install it too.
1. Place the scripts somewhere in your `$PATH`.
2. Fill in your credentials in `_todo-base` script.
3. Fill in your personal destination address in `todo`.
4. Fill in your work destination address in `todo-work`.
5. Assign `todo-gui` and `todo-work-gui` scripts to preferred shortcuts in you Desktop Environment.
