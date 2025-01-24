package me.kumo.drone.styles

import com.simsilica.lemur.*;
import com.simsilica.lemur.Button.ButtonAction;
import com.simsilica.lemur.component.*;

// Define a simple solid color background
def solidBackground = new QuadBackgroundComponent(color(0.2, 0.2, 0.2, 1)) // Dark grey

// Define the primary color (blue)
def primaryColor = color(0.25, 0.62, 1, 1) // #409EFF in RGBA

// Define a lighter grey for containers and backgrounds
def lightGrey = color(0.3, 0.3, 0.3, 1)

// Define white for text and highlights
def white = color(1, 1, 1, 1)

selector("kumo") {
    fontSize = 14
}

selector("label", "kumo") {
    insets = new Insets3f(2, 2, 0, 2)
    color = white
}

selector("container", "kumo") {
    background = solidBackground.clone()
    background.setColor(lightGrey)
}

selector("slider", "kumo") {
    background = solidBackground.clone()
    background.setColor(lightGrey)
}

def pressedCommand = new Command<Button>() {
    public void execute(Button source) {
        if (source.isPressed()) {
            source.move(1, -1, 0)
        } else {
            source.move(-1, 1, 0)
        }
    }
}

def repeatCommand = new Command<Button>() {
    private long startTime
    private long lastClick

    public void execute(Button source) {
        if (source.isPressed() && source.isHighlightOn()) {
            long elapsedTime = System.currentTimeMillis() - startTime
            if (elapsedTime > 500) {
                if (elapsedTime - lastClick > 125) {
                    source.click()
                    lastClick = ((elapsedTime - 500) / 125) * 125 + 500
                }
            }
        } else {
            startTime = System.currentTimeMillis()
            lastClick = 0
        }
    }
}

def stdButtonCommands = [
        (ButtonAction.Down): [pressedCommand],
        (ButtonAction.Up)  : [pressedCommand]
]

def sliderButtonCommands = [
        (ButtonAction.Hover): [repeatCommand]
]

selector("title", "kumo") {
    color = white
    highlightColor = primaryColor
    shadowColor = color(0, 0, 0, 0.75)
    shadowOffset = new com.jme3.math.Vector3f(2, -2, -1)
    background = solidBackground.clone()
    background.setColor(lightGrey)
    insets = new Insets3f(2, 2, 2, 2)

    buttonCommands = stdButtonCommands
}

selector("button", "kumo") {
    background = solidBackground.clone()
    color = white
    background.setColor(lightGrey)
    insets = new Insets3f(2, 2, 2, 2)

    buttonCommands = stdButtonCommands
}

selector("slider", "kumo") {
    insets = new Insets3f(1, 3, 1, 2)
}

selector("slider", "button", "kumo") {
    background = solidBackground.clone()
    background.setColor(lightGrey)
    insets = new Insets3f(0, 0, 0, 0)
}

selector("slider.thumb.button", "kumo") {
    text = "[]"
    color = primaryColor
}

selector("slider.left.button", "kumo") {
    text = "-"
    background = solidBackground.clone()
    background.setColor(lightGrey)
    background.setMargin(5, 0)
    color = primaryColor

    buttonCommands = sliderButtonCommands
}

selector("slider.right.button", "kumo") {
    text = "+"
    background = solidBackground.clone()
    background.setColor(lightGrey)
    background.setMargin(4, 0)
    color = primaryColor

    buttonCommands = sliderButtonCommands
}

selector("slider.up.button", "kumo") {
    buttonCommands = sliderButtonCommands
}

selector("slider.down.button", "kumo") {
    buttonCommands = sliderButtonCommands
}

selector("checkbox", "kumo") {
    def on = new IconComponent("/com/simsilica/lemur/icons/Glass-check-on.png", 1f, 0, 0, 1f, false)
    on.setColor(primaryColor)
    on.setMargin(5, 0)
    def off = new IconComponent("/com/simsilica/lemur/icons/Glass-check-off.png", 1f, 0, 0, 1f, false)
    off.setColor(lightGrey)
    off.setMargin(5, 0)

    onView = on
    offView = off

    color = white
}

selector("rollup", "kumo") {
    background = solidBackground.clone()
    background.setColor(lightGrey)
}

selector("tabbedPanel", "kumo") {
    activationColor = primaryColor
}

selector("tabbedPanel.container", "kumo") {
    background = null
}

selector("tab.button", "kumo") {
    background = solidBackground.clone()
    background.setColor(lightGrey)
    color = white
    insets = new Insets3f(4, 2, 0, 2)

    buttonCommands = stdButtonCommands
}