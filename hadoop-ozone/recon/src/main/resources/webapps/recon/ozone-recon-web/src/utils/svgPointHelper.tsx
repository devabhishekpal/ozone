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
 */

export const getSVGMousePoint = (svg: SVGSVGElement, event: MouseEvent) => {
  /**
  * Helper to return the x and y coordinates for the mouse pointer
  * relative to the SVG root container
  * @param svg   Stores the reference to the SVG element
  * @param event Stores the mouse event associated with the element
  */
  //  SVGPoint APIs might get deprecated according to MSN Docs, so safely check if API is available
  if (svg.createSVGPoint){
    let point = svg.createSVGPoint();
    point.x = event.clientX;
    point.y = event.clientY;
    point = point.matrixTransform(svg.getScreenCTM()!.inverse());
    return [point.x, point.y];
  }
  // If SVGPoint API is not available
  const rect = svg.getBoundingClientRect();
  const finalX = event.clientX - rect.left - svg.clientLeft;
  const finalY = event.clientY - rect.top - svg.clientTop;
  console.log(finalX + ", " + finalY)
  return [finalX, finalY]
}