# voyage

Voyage is a lightweight nostr client for Android with a Reddit-like UI. It is a fork and successor
of [Nozzle](https://github.com/dluvian/Nozzle).

[App icon source](https://www.flaticon.com/free-icons/greek)

## Installation

Install it
via [zap.store](https://zap.store/download), [Obtainium](https://github.com/ImranR98/Obtainium) or
go to
the [release page](https://github.com/dluvian/voyage/releases) and download the latest apk file.

I don't plan to publish this app in the Google Play Store.

## Some points of difference

- Outbox model: Voyage discovers relays through nip-65 events and encoded relays in
  nprofiles/nevents. It fetches data only from relays expected to contain the desired information.
- Sign-in only via an external signer.
- Reddit-like UI.
- App can be used offline because posts are stored locally.
- Mobile data friendly: Only text is displayed, no videos and no pictures. It also attempts to keep
  relay connections to a minimum, fetch events only from their respective outbox and not refetch
  locally cached events.
- Trust indicator: Each profile has a colored badge which indicates their trust level.
  - Green star: Yourself
  - Green check: You follow this profile (friend).
  - Orange check: You don't follow this profile, but at least one of your friends does.
  - Grey question mark: You don't follow this profile and neither do any of your friends.
- Apk size of less than 15MB.

## Supported (and partially supported) nips

- [NIP-01: Basic protocol flow description](https://github.com/nostr-protocol/nips/blob/master/01.md)
- [NIP-02: Follow List](https://github.com/nostr-protocol/nips/blob/master/02.md)
- [NIP-06: Basic key derivation from mnemonic seed phrase](https://github.com/nostr-protocol/nips/blob/master/06.md)
- [NIP-09: Event Deletion](https://github.com/nostr-protocol/nips/blob/master/09.md)
- [NIP-10: Conventions for clients' use of e and p tags in text events](https://github.com/nostr-protocol/nips/blob/master/10.md)
- [NIP-11: Relay Information Document](https://github.com/nostr-protocol/nips/blob/master/11.md)
- [NIP-14: Subject tag in text events](https://github.com/nostr-protocol/nips/blob/master/14.md)
- [NIP-18: Reposts](https://github.com/nostr-protocol/nips/blob/master/18.md)
- [NIP-19: bech32-encoded entities](https://github.com/nostr-protocol/nips/blob/master/19.md)
- [NIP-24: Extra metadata fields and tags](https://github.com/nostr-protocol/nips/blob/master/24.md)
- [NIP-25: Reactions](https://github.com/nostr-protocol/nips/blob/master/25.md)
- [NIP-27: Text Note References](https://github.com/nostr-protocol/nips/blob/master/27.md)
- [NIP-42: Authentication of clients to relays](https://github.com/nostr-protocol/nips/blob/master/42.md)
- [NIP-51: Lists](https://github.com/nostr-protocol/nips/blob/master/51.md)
- [NIP-55: Android Signer Application](https://github.com/nostr-protocol/nips/blob/master/55.md)
- [NIP-65: Relay List Metadata](https://github.com/nostr-protocol/nips/blob/master/65.md)

## Screenshots

<p>
<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/home_feed.png" width="24%" height="24%" />
<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/thread.png" width="24%" height="24%" />
<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/discover.png" width="24%" height="24%" />
<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/create_post.png" width="24%" height="24%" />
</p>

## License

[MIT licence](https://github.com/dluvian/voyage/blob/master/LICENSE)

## Resources

- Our external signer of choice is [Amber](https://github.com/greenart7c3/Amber)
- The project uses [rust-nostr](https://github.com/rust-nostr/nostr), a cross platform Nostr library
  written in Rust
- Release notes are generated with [git-cliff](https://github.com/orhun/git-cliff)

## Nostr

Find me on nostr:

npub1useke4f9maul5nf67dj0m9sq6jcsmnjzzk4ycvldwl4qss35fvgqjdk5ks

nprofile1qqswgvmv65ja7706f5a0xe8ajcqdfvgdeeppt2jvx0kh06sggg6ykyqpp4mhxue69uhkummn9ekx7mqpzamhxue69uhkummnw3ezuendwsh8w6t69e3xj7spremhxue69uhkummnw3ezuum9w35xvmmjwpexjanpvdujucm0d5q3camnwvaz7tmjv4kxz7fwd46hg6tw09mkzmrvv46zucm0d5q36amnwvaz7tmjv4kxz7fwdehhxarj9emkjun9v3hx2apwdfcqev7j8c
