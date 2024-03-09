package com.dluvian.voyage.data.provider

import rust.nostr.protocol.PublicKey

class FriendProvider {
    init {
        // TODO: insert defaultFriends with createdAt=0
        // TODO: subscribe my contactlist
    }

    fun getFriendPubkeys(): List<PublicKey> {
        // TODO: get from dao
        return defaultFriends.map { PublicKey.fromHex(it) }
    }
}

private val defaultFriends = listOf(
    // dluvian
    "e4336cd525df79fa4d3af364fd9600d4b10dce4215aa4c33ed77ea0842344b10",
    // fiatjaf
    "3bf0c63fcb93463407af97a5e5ee64fa883d107ef9e558472c4eb9aaaefa459d",
    // mikedilger
    "ee11a5dff40c19a555f41fe42b48f00e618c91225622ae37b6c2bb67b76c4e49",
    // hodlbod
    "97c70a44366a6535c145b333f973ea86dfdc2d7a99da618c40c64705ad98e322",
    // jack
    "82341f882b6eabcd2ba7f1ef90aad961cf074af15b9ef44a09f9d2a8fbfbe6a2",
    // odell
    "04c915daefee38317fa734444acee390a8269fe5810b2241e5e6dd343dfbecc9",
    // pablof7z
    "fa984bd7dbb282f07e16e7ae87b26a2a7b9b90b7246a44771f0cf5ae58018f52",
    // gigi
    "6e468422dfb74a5738702a8823b9b28168abab8655faacb6853cd0ee15deee93",
    // jb55
    "32e1827635450ebb3c5a7d12c1f8e7b2b514439ac10a67eef3d9fd9c5c68e245",
    // kieran
    "63fe6318dc58583cfe16810f86dd09e18bfd76aabc24a0081ce2856f330504ed",
    // karnage
    "1bc70a0148b3f316da33fe3c89f23e3e71ac4ff998027ec712b905cd24f6a411",
    // nostreport
    "2edbcea694d164629854a52583458fd6d965b161e3c48b57d3aff01940558884",
    // fishcake
    "8fb140b4e8ddef97ce4b821d247278a1a4353362623f64021484b372f948000c",
    // lynalden
    "eab0e756d32b80bcd464f3d844b8040303075a13eabc3599a762c9ac7ab91f4f",
    // yonle
    "347a2370900d19b4e4756221594e8bda706ae5c785de09e59e4605f91a03f49c",
    // gladstein
    "58c741aa630c2da35a56a77c1d05381908bd10504fdd2d8b43f725efa6d23196",
    // tanel
    "5c508c34f58866ec7341aaf10cc1af52e9232bb9f859c8103ca5ecf2aa93bf78",
    // yukikishimoto
    "68d81165918100b7da43fc28f7d1fc12554466e1115886b9e7bb326f65ec4272",
    // unclebob
    "2ef93f01cd2493e04235a6b87b10d3c4a74e2a7eb7c3caf168268f6af73314b5",
    // greenart7c3
    "7579076d9aff0a4cfdefa7e2045f2486c7e5d8bc63bfc6b45397233e1bbfcb19",
    // preston
    "85080d3bad70ccdcd7f74c29a44f55bb85cbcd3dd0cbb957da1d215bdb931204",
)
