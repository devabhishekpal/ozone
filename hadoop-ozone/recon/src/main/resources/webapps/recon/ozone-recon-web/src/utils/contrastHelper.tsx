/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Calculations and formulae used from w3 documentation for contrast
 * Ref: G17: Ensuring that a contrast ratio of at least 7:1 exists between text
 * (and images of text) and background behind the text
 */

const hexToRGB = (hex: string) => {
  /**
   * This utility method will return the (R, G, B) representation
   * of a color from it's Hex value
   * @param hex Stores the hex value for the color
   */
  var shorthandRegex = /^#?([a-f\d])([a-f\d])([a-f\d])$/i;
  hex = hex.replace(shorthandRegex, (r, g, b) => {
    return r + r + g + g + b + b;
  });

  var result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
  return result ? {
    r: parseInt(result[1], 16),
    g: parseInt(result[2], 16),
    b: parseInt(result[3], 16)
  } : null;
}

const rgbToLuminance = (r: number, g: number, b: number) => {
  /**
   * This utility method will return the luminance for the provided
   * R, G and B values
   * @param r Stores the value for Red which will be between 0 - 255
   * @param g Stores the value for Green which will be between 0 - 255
   * @param b Stores the value for Blue which will be between 0 - 255
  */
  var relativeLuminance = [r, g, b].map(function (luminance) {
    luminance /= 255;
    return luminance <= 0.03928
        ? luminance / 12.92
        : Math.pow((luminance + 0.055) / 1.055, 2.4);
  });
  return relativeLuminance[0] * 0.2126
    + relativeLuminance[1] * 0.7152
    + relativeLuminance[2] * 0.0722;
}

export const getContrastRatioForColor = (bgColor: string, fgColor: string) => {
  /**
   * This utility method will compare the contrast between two colours
   * namely fgColor and bgColor
   * @param bgColor Stores the first color to compare against for contrast
   * @param fgColor Stores the second color to compare for contrast
  */
  const bgRGB = hexToRGB(bgColor);
  const fgRGB = hexToRGB(fgColor);
  const bgLuminance = rgbToLuminance(bgRGB!.r, bgRGB!.g, bgRGB!.b);
  const fgLuminance = rgbToLuminance(fgRGB!.r, fgRGB!.g, fgRGB!.b);
  return (bgLuminance > fgLuminance)
         ? ((fgLuminance + 0.05) / (bgLuminance + 0.05))
         : ((bgLuminance + 0.05) / (fgLuminance + 0.05));
}