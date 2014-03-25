Offspring
=========

NXT crypto currency desktop client. 

Offspring uses the NXT Java API directly, this allows for fast access to all data in the blockchain. All displayed transactions, accounts and blocks can be clicked and inspected.

Features
========

* Asset Exchange (fully functional, buy/sell assets, issue assets etc..)
* Block Explorer
* Peer Explorer
* Account Explorer/Inspector
* Transaction Explorer/Inspector
* Send and receive NXT
* Send and receive Assets
* Register/Explore/Update Aliases

Requirements
============

Java 1.7 is required. Cross platform (Windows, Mac and Linux).

Install
=======

Go to http://offspring.dgex.com and download the version for your platform.

Build
=====

Offspring builds for all platforms at once (only tried this on ubuntu).

Clone this repository

```
$ git clone https://github.com/incentivetoken/offspring.git
```

Run maven

```
$ cd [CLONE DIR]/com.dgex.offspring.master
$ mvn clean verify
```

Find the installers in:

```
[CLONE DIR]/com.dgex.offspring.product/target/products/..
```
