# _Readers2016_

_Readers2016_ is a configurable sampler of three texts and selected quasi-autonomous, algorithmic Readers. Applications for Mac OS X, Windows and Unix allow human readers to shape and share reading experiences with various strategies and visual behaviors. These experiences are embodied in and played out by Readers that have been developed as a part of _The Readers Project_ <http://thereadersproject.org>.

  When you double-click to open the _Readers2016_ application you will see the first two ‘pages’ (of three) setting out the text of 'Poetic Caption.' After a short delay a Perigram Reader begins to move from the first top-left word of this text, highlighting what it attends to as it progresses.

## General Controls

At any time you can hit the `SPACE` bar to pause or restart the active Reader. You can also, at any time, press the `N` (for next) or `right-arrow key` in order to make a 'next' page of text visible. But note that because this version of _Readers2016_ only allows one Reader to be active, making next pages visible may obscure an earlier page which carries the active Reader. If this happens you may, simply, keep on hitting `N` or right-arrow until you cycle back.

## Cycling Buttons for Configuration

If your cursor is positioned over the lowest register of the application, a set of labeled cycling buttons becomes visible. These are 'cycling' in the sense that successive mouse clicks will cycle through a short list of possible values. Hold down the shift key to cycle in the other direction, for example, typically, to return to a previous value.

  The cursor in this position also reveals a reading 'monitor' positioned towards the bottom right. This displays, 'live,' the word that is currently at the center of the active Reader's attention.

  Configuration of _Readers2016_ and certain underlying principles of _The Readers Project_ will be explained by going through each of the cycling buttons in turn.

### _Title_ Button
Clicking successively on this button cycles through the texts available:

* 'Poetic Caption' by John Cayley, 3 pp.
* 'Misspelt Langdings' by John Cayley, 3 pp.
* 'The Image' from _How It Is_ by Samuel Beckett, 6 pp.

### _Text_ Button (the text's default visibility)

The options here (initial value first) are:

* Faint - the text is dark gray
* Dark - the underlying text is blacked out
* Gray - the underlying text is gray (offering greater readability but less contrast with the Reader’s non-focal visual behaviors)

### _Reader_ Button

Three distinct types of Reader are implemented in _Readers2016_. Further information on these readers and how they operate is available on _The Readers Project_ website.

  The ‘Reader’ button allows you to cycle through the three types, with each type pre-configured by the authors of _The Readers Project._ The options are:

* Perigram [Reader]
* Less Directed Perigram [Reader]
* Simple Spawner
* Perigram Spawner
* Less Directed [Perigram] Spawner
* Mesostic [Reader]

All Readers are aware of their typographic neighborhood: the words that surround them on their 2D reading surfaces. Neighboring words are distinguished using directional terms relative to the current, central word being 'read.' The next word to be read in conventional order is said to be 'East' of the current, central word. In principle, a Reader may choose to shift its attention to any neighboring word, so delineated. In practice, most Readers pay more attention to words adjacent to the conventional vector of human reading: NE north-east and SE south-east.

All readers in this selection are also provided with information concerning the relative frequencies of potential three-word phrases (3-grams) in their typographic neighborhoods. These we call these Perigrams. [NOTE]

The three main Reader types in _Readers2016_:

* **Perigram Reader**  
A Perigram Reader determines its direction of reading – or the next word to which it will shift its attention – by assessing whether on not a word in its typographic neighborhood completes a relatively frequent Perigram (see above). A Pergram reader is, in a sense, like a 3-gram Markov chain text generator that remains tethered to its typographic locale.

* **Spawner**  
A Spawner or Spawning Reader will shift its chief point of attention through the text in a number of different ways (see below), but it will also, by definition, spawn (in _Readers2016_) uni-directional Perigram Readers in either a north-eastern or a south-eastern direction, or both. In principle any kind of Reader might spawn Readers with their own characteristics and behaviors, including other Spawning Readers.

* **Mesostic Reader**  
The Mesostic Reader is quite different. It is pre-seeded with a piece of language that it must spell out by finding, in succession, words within the text that it is reading which contain the letters of its seed. This Reader endlessly loops around this task of spelling.  
  Mesostic Readers also have relative frequency information concerning the Perigrams of the text that they are reading. They look for the nearest next word that allows them to spell their seed phrases, but they prefer to find words that complete a relative frequent Perigram.

Now, to expand on the options actually implemented for _Readers2016_ : 

* **Perigram [Reader]**  
The Perigram Reader, as configured for _The Readers Project_ and _Readers2016_ is one that determines whether on not to shift its attention from a conventional reading order depending on whether on not it finds relatively frequent completions of three-word Perigrams in, especially, its north-eastern and, more especially, its south-eastern neighbors. The attention of this Reader wanders but tends to proceed in a conventional overall direction, weighted to the south-east.

* **Less Directed Perigram Reader**  
This Reader also only considers reading pathways where it may complete a relatively frequent Perigram, but its weightings allow it to shift attention upwards, downwards and even backwards with respect to the conventional path of human reading.

* **Simple Spawner**  
The reading path of this Reader is ‘simple’ or convential. It always moves its center of attention on to the next easterly word, but as it does do it will spawn readers in either or both south-eastern or north-eastern directions if these words complete a relatively frequent Perigram. The directions in which spawned Readers will be set off is determined by the value of the ‘Spawning’ button.

* **Perigram Spawner**  
This reader behaves in the same way as a Simple Spawner but its center of attention moves in the same way as _Readers2016_’s Perigram Reader.

* **Less Directed Perigram Spawner**  
This reader behaves in the same way as a Simple Spawner but its center of attention moves in the same way as _Readers2016_’s Less Directed Perigram Reader.

* **Mesostic [Reader]**  
Descibed above under ‘Mesostic Reader.’ For 'Poetic Caption' the seed text is 'READING AS WRITING THROUGH', for 'Misspelt Landings' the text is 'REACHING OUT FALLING THROUGH CIRCLING OVER LANDING ON TURNING WITHIN SPELLING AS' and for 'The Image' it is 'COMES IN IS OVER GOES OUT IS DONE LOLLS IN STAYS THERE IS HAD NO MORE.'

### _Color_ Button

This button sets the color of a word while it is at the center of a Reader's attention. The values are:

* White
* Yellow
* Orange
* Orche
* Brown

### _Speed_ Button

Sets a speed at which the Reader's center of attention moves from word to word:

* Fluent
* Steady
* Slow
* Slower
* Slowest
* Fast

### _Visual_ Button

Toggles between one of two characteristic forms adopted by the Reader's visual behavior:

* Traces - neighboring or once-neighboring words that might be or have been read are partially highlighted, before fading to the text’s default
* Haloes - neighboring words as above may be entirely faded out to form a halo around the Reader's center of attention

### _Spawning_ Button

Only applies to Spawners and determines the vector of the uni-directional Perigram Readers that are spawned by the primary Reader. Spawners may be configured to set off in the following directions:

* NE & SE - both north-east and south-east
* South-East
* North-East

These readers are only spawned if a word in the direction indicated completes a Perigram and they 'die' when then can no long find a Perigram completion in their direction of reading.

---
**[NOTE]** Perigrams are  a large subset of all possible 3-word sequences that could be generated from all the unique words of a text for reading. Only combinations of words that are found within twenty words (long sentence length) of every possible linear human reading word-position are allowed for the formation of Perigrams. Once the set of Perigrams for a text are determined they are then provided with relative frequency information. For the texts in _Readers2016_ relative frequency information was mined from the word-counts of live Google searches for the Perigrams double-quoted (verbatim in Google’s terminology).