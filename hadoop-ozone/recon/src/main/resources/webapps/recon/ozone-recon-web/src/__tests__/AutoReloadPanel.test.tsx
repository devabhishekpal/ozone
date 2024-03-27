import React from "react";
import { BrowserRouter } from "react-router-dom";

/**
 * The dont-cleanup-after-each is imported to prevent autoCleanup of rendered
 * component after each test.
 * Since we are needing to set a timeout everytime the component is rendered
 * and we would be verifying whether the UI is correct, we can skip the cleanup
 * and leave it for after all the tests run - saving test time
 */
import "@testing-library/react/dont-cleanup-after-each";
import { fireEvent, render, screen } from "@testing-library/react";

import { autoReloadPanelLocators } from "./locators/locators";
import AutoReloadPanel from "components/autoReloadPanel/autoReloadPanel";

const WrappedAutoReloadComponent = () => {
  return (
    <BrowserRouter>
      <AutoReloadPanel togglePolling={jest.fn()} lastRefreshed={10}/>
    </BrowserRouter>
  )
}


describe("Auto-Reload Panel Tests", () => {
  beforeAll(() => {
    render(
      <WrappedAutoReloadComponent/>
    );
  });

  // Tests begin here
  // All the data is being mocked by MSW, so we have a fixed data that we can verify
  // the content against
  it("Auto Reload panel switch can be toggled", async () => {
    const toggleSwitch = screen.getByTestId(autoReloadPanelLocators.toggleSwitch)
    let currentSwitchStat = toggleSwitch.getAttribute("aria-checked") === "false" ? false : true;
    expect(toggleSwitch).toBeVisible();
    await fireEvent.click(toggleSwitch);
    currentSwitchStat = !currentSwitchStat
    expect(toggleSwitch.getAttribute("aria-checked")).toEqual(currentSwitchStat.toString());
  });

  it("Auto Reload Last Refreshed text is properly displayed", async () => {
    const reloadPanel = screen.getByTestId(autoReloadPanelLocators.autoreloadPanel)
    expect(reloadPanel).toHaveTextContent("Refreshed at 5:30 AM")
  });
});